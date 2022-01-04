/*
 * Testerra Xray-Connector
 *
 * (C) 2020, Martin Cölln, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.XrayConnector;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraKeyReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import eu.tsystems.mms.tic.testframework.events.TestStatusUpdateEvent;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;


public abstract class AbstractXrayResultsSynchronizer implements XrayResultsSynchronizer, Loggable, TestStatusUpdateEvent.Listener {
    private static final String VENDOR_PREFIX="Testerra Xray connector";
    protected final XrayConfig xrayConfig = XrayConfig.getInstance();
    private boolean isSyncInitialized = false;
    private XrayTestExecutionIssue testExecutionIssue;
    private XrayMapper xrayMapper;
    private final XrayConnector connector = new XrayConnector(new XrayInfo());
    private final XrayUtils xrayUtils = new XrayUtils(connector.getWebResource());
    private final HashMap<String, XrayTestSetIssue> testSetCacheByClassName = new HashMap<>();
    private final HashMap<String, JiraIssue> testCacheByMethodName = new HashMap<>();
    private final HashMap<String, JiraIssue> testCacheByKey = new HashMap<>();
    private final ConcurrentLinkedQueue<XrayTestExecutionImport.Test> testSyncQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<XrayTestSetIssue> testSetSyncQueue = new ConcurrentLinkedQueue<>();

    public void initialize() {

        if (xrayConfig.isSyncEnabled()) {
            try {
                XrayTestExecutionIssue testExecutionIssue = new XrayTestExecutionIssue();
                testExecutionIssue.getProject().setKey(xrayConfig.getProjectKey());

                Optional<String> optionalSummary;
                Optional<String> optionalDescription;
                Optional<String> optionalRevision;

                final XrayTestExecutionInfo executionInfo = getExecutionInfo();
                if (executionInfo != null) {
                    optionalSummary = Optional.ofNullable(executionInfo.getSummary());
                    optionalDescription = Optional.ofNullable(executionInfo.getDescription());
                    optionalRevision = Optional.ofNullable(executionInfo.getRevision());
                    testExecutionIssue.getAssignee().setName(executionInfo.getAssignee());
                    testExecutionIssue.setVersions(Collections.singletonList(new JiraNameReference(executionInfo.getFixVersion())));
                    testExecutionIssue.setTestEnvironments(executionInfo.getTestEnvironments());
                } else {
                    optionalSummary = Optional.empty();
                    optionalDescription = Optional.empty();
                    optionalRevision = Optional.empty();
                }

                this.xrayMapper = Optional.ofNullable(getXrayMapper()).orElse(new EmptyMapper());

                testExecutionIssue.setSummary(optionalSummary.orElse(String.format("%s automated TestExecution", VENDOR_PREFIX)));
                testExecutionIssue.setDescription(optionalDescription.orElse(String.format("This is an automated import of a TestExecution generated by the %s", VENDOR_PREFIX)));
                testExecutionIssue.setRevision(optionalRevision.orElse(new Date().toString()));
                xrayMapper.updateXrayTestExecution(testExecutionIssue);

                Optional<XrayTestExecutionIssue> optionalExistingTestExecution = xrayMapper.createXrayTestExecutionQuery(testExecutionIssue)
                        .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());

                if (optionalExistingTestExecution.isPresent()) {
                    testExecutionIssue = optionalExistingTestExecution.get();
                    xrayMapper.updateXrayTestExecution(testExecutionIssue);
                }
                this.testExecutionIssue = testExecutionIssue;
                isSyncInitialized = true;
            } catch (final Exception e) {
                disableSyncWithWarning(e);
            }
        }
    }

    public void shutdown() {
        this.flushSyncQueue();
    }

    private synchronized void flushSyncQueue() {

        if (!isSyncInitialized) {
            return;
        }

        testSetSyncQueue.forEach(xrayTestSetIssue -> {
            try {
                xrayUtils.createOrUpdateIssue(xrayTestSetIssue);
            } catch (IOException e) {
                log().error("Unable to update TestSet", e);
            }
            testSetSyncQueue.remove(xrayTestSetIssue);
        });

        XrayTestExecutionImport xrayTestExecutionImport = new XrayTestExecutionImport(this.testExecutionIssue);
        testSyncQueue.forEach(test -> {
            xrayTestExecutionImport.addTest(test);
            testSyncQueue.remove(test);
        });

        if (this.getExecutionUpdates() != null) {
            log().warn(String.format("getExecutionUpdates() is ignored. Please use updateXrayTestExecution(%s) instead", XrayTestExecutionIssue.class.getSimpleName()));
        }

        try {
            xrayUtils.importTestExecution(xrayTestExecutionImport);
        } catch (IOException e) {
            log().error("Unable to update TestExecution", e);
        }
    }

    @Override
    public XrayTestExecutionInfo getExecutionInfo() {
        return null;
    }

    @Override
    public XrayMapper getXrayMapper() {
        return null;
    }

    @Override
    public XrayTestExecutionUpdates getExecutionUpdates() {
        return null;
    }

    private void reportError(String message, Exception e) {
        //ReportInfo.getDashboardInfo().addInfo(1, message + e.getMessage());
        log().error(message, e);
    }

    private void disableSyncWithWarning(Exception e) {
        isSyncInitialized = false;
        reportError("An unexpected exception occurred. Syncing is aborted.", e);
    }

    @Override
    public void onTestStatusUpdate(TestStatusUpdateEvent event) {
        Optional<ITestResult> testNgResult = event.getMethodContext().getTestNgResult();
        if (!testNgResult.isPresent()) {
            return;
        }

        ITestResult testResult = testNgResult.get();
        MethodContext methodContext = event.getMethodContext();

        Set<JiraIssue> testIssues = getTestIssues(testResult.getMethod());
        if (testIssues.isEmpty()) {

            final String cacheKey = testResult.getMethod().getQualifiedName();

            if (!testCacheByMethodName.containsKey(cacheKey)) {
                Optional<JqlQuery> xrayTestQuery = xrayMapper.createXrayTestQuery(methodContext);
                if (xrayTestQuery.isPresent()) {
                    Optional<JiraIssue> optionalExistingTestIssue = xrayUtils.searchIssues(xrayTestQuery.get()).findFirst();
                    if (optionalExistingTestIssue.isPresent()) {
                        testCacheByMethodName.put(cacheKey, optionalExistingTestIssue.get());
                    } else {
                        JiraIssue testIssue = new JiraIssue();
                        testIssue.getProject().setKey(xrayConfig.getProjectKey());
                        testIssue.setSummary(testResult.getMethod().getQualifiedName());
                        testIssue.setDescription(String.format("%s generated Test by method %s", VENDOR_PREFIX, testResult.getMethod().getQualifiedName()));
                        testCacheByMethodName.put(cacheKey, testIssue);
                    }
                }
            }

            if (testCacheByMethodName.containsKey(cacheKey)) {
                testIssues.add(testCacheByMethodName.get(cacheKey));
            }
        }


        getTestSetIssue(methodContext.getClassContext()).ifPresent(xrayTestSetIssue -> {
            xrayMapper.updateXrayTestSet(xrayTestSetIssue, methodContext.getClassContext());

            List<String> testSetTestKeys = xrayTestSetIssue.getTestKeys();
            List<String> newTestKeys = testIssues.stream()
                    .map(JiraKeyReference::getKey)
                    .filter(testKey -> !testSetTestKeys.contains(testKey))
                    .collect(Collectors.toList());

            if (newTestKeys.size() > 0) {
                testSetTestKeys.addAll(newTestKeys);
                testSetSyncQueue.add(xrayTestSetIssue);
            }
        });


        testIssues.stream()
                .peek(issue -> xrayMapper.updateXrayTest(issue, methodContext))
                .map(XrayTestExecutionImport.Test::new)
                .peek(test -> updateTestImport(test, testResult))
                .forEach(testSyncQueue::add);
    }

    private void updateTestImport(XrayTestExecutionImport.Test test, ITestResult result) {
        test.setStart(new Date(result.getStartMillis()));

        switch (result.getStatus()) {
            case ITestResult.FAILURE:
                test.setStatus(XrayTestExecutionImport.Test.Status.FAIL);
                break;
            case ITestResult.SUCCESS:
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                test.setStatus(XrayTestExecutionImport.Test.Status.PASS);
                break;
            case ITestResult.SKIP:
                test.setStart(Calendar.getInstance().getTime());
                test.setStatus(XrayTestExecutionImport.Test.Status.SKIPPED);
                break;
            default:
                log().error("TestNg result status {} cannot be processed", result.getStatus());
        }

        test.setFinish(new Date(result.getEndMillis()));
    }

    private Set<JiraIssue> getTestIssues(ITestNGMethod testNGMethod) {
        final Method method = testNGMethod.getConstructorOrMethod().getMethod();
        if (!method.isAnnotationPresent(XrayTest.class)) {
            return new HashSet<>();
        }

        // Test set key is present
        String[] testKeys = method.getAnnotation(XrayTest.class).key();

        // Load all unloaded test issues
        Arrays.stream(testKeys)
                .filter(StringUtils::isNotBlank)
                .filter(testKey -> !testCacheByKey.containsKey(testKey))
                .map(testKey -> {
                    try {
                        return xrayUtils.getIssue(testKey);
                    } catch (IOException e) {
                        log().error(String.format("Unable to query Test by key: %s", testKey), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(issue -> testCacheByKey.put(issue.getKey(), issue));

        return Arrays.stream(testKeys)
                .filter(testCacheByKey::containsKey)
                .map(testCacheByKey::get)
                .collect(Collectors.toSet());
    }

    private Optional<XrayTestSetIssue> getTestSetIssue(ClassContext classContext) {
        Class<?> clazz = classContext.getTestClass();

        if (!clazz.isAnnotationPresent(XrayTestSet.class)) {
            return Optional.empty();
        }

        String cacheKey = clazz.getCanonicalName();
        if (this.testSetCacheByClassName.containsKey(cacheKey)) {
            // Cache could be null
            return Optional.ofNullable(this.testSetCacheByClassName.get(cacheKey));
        }

        XrayTestSetIssue xrayTestSetIssue = null;

        // Test set key is present
        String testSetKey = clazz.getAnnotation(XrayTestSet.class).key();
        if (StringUtils.isNotBlank(testSetKey)) {
            try {
                xrayTestSetIssue = xrayUtils.getIssue(testSetKey, XrayTestSetIssue::new);
            } catch (IOException e) {
                log().error(String.format("Unable to query TestSet by key: %s", testSetKey), e);
            }
        } else {
            Optional<JqlQuery> xrayTestSetQuery = xrayMapper.createXrayTestSetQuery(classContext);
            if (xrayTestSetQuery.isPresent()) {
                Optional<XrayTestSetIssue> optionalExistingTestSetIssue = xrayUtils.searchIssues(xrayTestSetQuery.get(), XrayTestSetIssue::new).findFirst();
                if (optionalExistingTestSetIssue.isPresent()) {
                    xrayTestSetIssue = optionalExistingTestSetIssue.get();
                } else {
                    xrayTestSetIssue = new XrayTestSetIssue();
                    xrayTestSetIssue.getProject().setKey(xrayConfig.getProjectKey());
                    xrayTestSetIssue.setSummary(clazz.getSimpleName());
                    xrayTestSetIssue.setDescription(String.format("%s generated TestSet by class %s", VENDOR_PREFIX, clazz.getCanonicalName()));
                }
            }
        }

        // cache could be null
        this.testSetCacheByClassName.put(cacheKey, xrayTestSetIssue);
        return Optional.ofNullable(xrayTestSetIssue);
    }
}
