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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.XrayConnector;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraKeyReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import eu.tsystems.mms.tic.testframework.events.TestStatusUpdateEvent;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.ExecutionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStep;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStepAction;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.testng.ITestResult;


public abstract class AbstractXrayResultsSynchronizer implements XrayResultsSynchronizer, Loggable, TestStatusUpdateEvent.Listener {
    private static final String VENDOR_PREFIX = "Testerra Xray connector";
    private boolean isSyncEnabled = false;
    private XrayTestExecutionIssue testExecutionIssue;
    private XrayMapper xrayMapper;
    private XrayUtils xrayUtils;
    private final HashMap<String, XrayTestSetIssue> testSetCacheByClassName = new HashMap<>();
    private final HashMap<String, JiraIssue> testCacheByMethodName = new HashMap<>();
    private final ConcurrentLinkedQueue<XrayTestExecutionImport.Test> testSyncQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<XrayTestSetIssue> testSetSyncQueue = new ConcurrentLinkedQueue<>();
    private final int SYNC_FREQUENCY_TESTS = 10;

    private XrayConfig getXrayConfig() {
        return XrayConfig.getInstance();
    }

    private XrayUtils getXrayUtils() {
        if (this.xrayUtils == null) {
            XrayConnector connector = new XrayConnector();
            this.xrayUtils = new XrayUtils(connector.getWebResource());
        }
        return this.xrayUtils;
    }

    private XrayTestExecutionIssue getTestExecutionIssue() {
        if (this.testExecutionIssue == null) {
            final XrayConfig xrayConfig = getXrayConfig();
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

            testExecutionIssue.setSummary(optionalSummary.orElse(String.format("%s automated %s", VENDOR_PREFIX, IssueType.TestExecution)));
            testExecutionIssue.setDescription(optionalDescription.orElse(String.format("This is an automated import of a %s generated by the %s", IssueType.TestExecution, VENDOR_PREFIX)));
            testExecutionIssue.setRevision(optionalRevision.orElse(new Date().toString()));

            final XrayMapper xrayMapper = getXrayMapper();
            final XrayUtils xrayUtils = getXrayUtils();
            final ExecutionContext executionContext = ExecutionContextController.getCurrentExecutionContext();
            xrayMapper.updateXrayTestExecution(testExecutionIssue, executionContext);

            Optional<XrayTestExecutionIssue> optionalExistingTestExecution = xrayMapper.createXrayTestExecutionQuery(testExecutionIssue)
                    .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());

            log().info(String.format("Using mapper %s", xrayMapper.getClass().getSimpleName()));

            if (optionalExistingTestExecution.isPresent()) {
                testExecutionIssue = optionalExistingTestExecution.get();
                log().info(String.format("Use existing %s (%s)",
                        IssueType.TestExecution,
                        xrayConfig.getIssueUrl(testExecutionIssue.getKey()).orElse(null)
                ));
                xrayMapper.updateXrayTestExecution(testExecutionIssue, executionContext);
            } else {
                log().info(String.format("Create new %s", IssueType.TestExecution));
            }

            this.testExecutionIssue = testExecutionIssue;
        }
        return this.testExecutionIssue;
    }

    @Override
    public void initialize() {
        EventBus eventBus = TesterraListener.getEventBus();
        eventBus.register(this);

        isSyncEnabled = true;
    }

    @Override
    public void shutdown() {
        flushSyncQueue();
        unregisterSync();
    }

    private synchronized void unregisterSync() {
        if (isSyncEnabled) {
            EventBus eventBus = TesterraListener.getEventBus();
            eventBus.unregister(this);

            isSyncEnabled = false;
        }
    }

    private synchronized void flushSyncQueue() {
        if (!isSyncEnabled) {
            return;
        }

        final int numTestSetsToSync = testSetSyncQueue.size();
        final int numTestsToSync = testSyncQueue.size();

        if (numTestSetsToSync == 0 && numTestsToSync == 0) {
            return;
        }

        final XrayUtils xrayUtils = getXrayUtils();

        testSetSyncQueue.forEach(xrayTestSetIssue -> {
            try {
                xrayUtils.createOrUpdateIssue(xrayTestSetIssue);
                Optional<URI> issueUrl = getXrayConfig().getIssueUrl(xrayTestSetIssue.getKey());
                log().info(String.format("Synchronized %s (%s) with %d %s",
                        IssueType.TestSet,
                        issueUrl.orElse(null),
                        xrayTestSetIssue.getTestKeys().size(),
                        IssueType.Test
                ));
            } catch (IOException e) {
                log().error(String.format("Unable to update %s", IssueType.TestSet), e);
            }
            testSetSyncQueue.remove(xrayTestSetIssue);
        });

        final XrayTestExecutionImport xrayTestExecutionImport = new XrayTestExecutionImport(getTestExecutionIssue());
        testSyncQueue.forEach(test -> {
            xrayTestExecutionImport.addTest(test);
            testSyncQueue.remove(test);
        });

        if (this.getExecutionUpdates() != null) {
            log().warn("getExecutionUpdates() is ignored");
        }

        try {
            xrayUtils.importTestExecution(xrayTestExecutionImport);
            Optional<URI> issueUrl = getXrayConfig().getIssueUrl(xrayTestExecutionImport.getTestExecutionKey());
            log().info(String.format("Synchronized %s (%s) with %d %s",
                    IssueType.TestExecution,
                    issueUrl.orElse(null),
                    numTestsToSync,
                    IssueType.Test
            ));
        } catch (IOException e) {
            log().error(String.format("Unable to synchronize %s", IssueType.TestExecution), e);
        }
    }

    @Override
    public XrayMapper getXrayMapper() {
        if (this.xrayMapper == null) {
            this.xrayMapper = new EmptyMapper();
        }
        return this.xrayMapper;
    }

    @Override
    @Subscribe
    public void onTestStatusUpdate(TestStatusUpdateEvent event) {
        final MethodContext methodContext = event.getMethodContext();
        Optional<ITestResult> testNgResult = methodContext.getTestNgResult();
        if (!testNgResult.isPresent()) {
            return;
        }

        final ITestResult testResult = testNgResult.get();
        final Method realMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        if (!realMethod.isAnnotationPresent(XrayTest.class)) {
            return;
        }

        final XrayMapper xrayMapper = getXrayMapper();
        final XrayUtils xrayUtils = getXrayUtils();
        final XrayConfig xrayConfig = getXrayConfig();

        Set<JiraIssue> testIssues = getTestIssues(realMethod);
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
                        testIssue.setDescription(String.format("%s generated %s by method %s", VENDOR_PREFIX, IssueType.Test, testResult.getMethod().getQualifiedName()));
                        testCacheByMethodName.put(cacheKey, testIssue);
                    }
                }
            }

            if (testCacheByMethodName.containsKey(cacheKey)) {
                testIssues.add(testCacheByMethodName.get(cacheKey));
            }
        }


        getTestSetIssue(methodContext.getClassContext()).ifPresent(xrayTestSetIssue -> {
            // Update test set by mapper
            xrayMapper.updateXrayTestSet(xrayTestSetIssue, methodContext.getClassContext());

            List<String> testSetTestKeys = xrayTestSetIssue.getTestKeys();
            List<String> newTestKeys = testIssues.stream()
                    .map(JiraKeyReference::getKey)
                    .filter(Objects::nonNull)
                    .filter(testKey -> !testSetTestKeys.contains(testKey))
                    .collect(Collectors.toList());

            // Add new tests to testset
            if (newTestKeys.size() > 0) {
                testSetTestKeys.addAll(newTestKeys);
                if (!testSetSyncQueue.contains(xrayTestSetIssue)) {
                    testSetSyncQueue.add(xrayTestSetIssue);
                }
            }
        });

        /*
         * Update test by mapper,
         * convert to import entity
         * and add to sync queue
         */
        testIssues.stream()
                .peek(issue -> xrayMapper.updateXrayTest(issue, methodContext))
                .map(XrayTestExecutionImport.Test::new)
                .peek(test -> updateTestImport(test, methodContext))
                .forEach(testSyncQueue::add);

        if (testSyncQueue.size() >= SYNC_FREQUENCY_TESTS) {
            flushSyncQueue();
        }
    }

    private void updateTestImport(XrayTestExecutionImport.Test test, MethodContext methodContext) {
        ITestResult testResult = methodContext.getTestNgResult().get();

        test.setStart(new Date(testResult.getStartMillis()));

        switch (testResult.getStatus()) {
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
                log().error("TestNg result status {} cannot be processed", testResult.getStatus());
        }

        test.setFinish(new Date(testResult.getEndMillis()));

        int lastFailedTestStepIndex = methodContext.getLastFailedTestStepIndex();
        List<TestStep> testSteps = methodContext.readTestSteps().collect(Collectors.toList());
        int i = 0;
        for (TestStep testStep : testSteps) {
            XrayTestExecutionImport.Test.Status testStepStatus = XrayTestExecutionImport.Test.Status.PASS;
            if (i == lastFailedTestStepIndex) {
                testStepStatus = XrayTestExecutionImport.Test.Status.FAIL;
            }

            XrayTestExecutionImport.Test.Step step2 = new XrayTestExecutionImport.Test.Step();
            step2.setStatus(testStepStatus);
            testStep.getTestStepActions().stream().flatMap(TestStepAction::readErrors).findFirst().ifPresent(errorContext -> {
                step2.setActualResult(errorContext.getThrowable().getMessage());
            });

            XrayTestExecutionImport.Test.Info.Step step = new XrayTestExecutionImport.Test.Info.Step();
            step.setAction(testStep.getName());
            step.setResult(testStepStatus.toString());

            test.getTestInfo().addStep(step);
            test.addStep(step2);
        }
    }

    private Set<JiraIssue> getTestIssues(Method realMethod) {
        // Test set key is present
        String[] testKeys = realMethod.getAnnotation(XrayTest.class).key();

        // Load all unloaded test issues
        return Arrays.stream(testKeys)
                .filter(StringUtils::isNotBlank)
                .map(JiraIssue::new)
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
        final XrayMapper xrayMapper = getXrayMapper();
        final XrayUtils xrayUtils = getXrayUtils();
        final XrayConfig xrayConfig = getXrayConfig();

        // Test set key is present
        String testSetKey = clazz.getAnnotation(XrayTestSet.class).key();
        if (StringUtils.isNotBlank(testSetKey)) {
            try {
                xrayTestSetIssue = xrayUtils.getIssue(testSetKey, XrayTestSetIssue::new);
            } catch (IOException e) {
                log().error(String.format("Unable to query %s by key: %s", IssueType.TestSet, testSetKey), e);
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
                    xrayTestSetIssue.setDescription(String.format("%s generated %s by class %s", VENDOR_PREFIX, IssueType.TestSet, clazz.getCanonicalName()));
                }
            }
        }

        // cache could be null
        this.testSetCacheByClassName.put(cacheKey, xrayTestSetIssue);
        return Optional.ofNullable(xrayTestSetIssue);
    }
}
