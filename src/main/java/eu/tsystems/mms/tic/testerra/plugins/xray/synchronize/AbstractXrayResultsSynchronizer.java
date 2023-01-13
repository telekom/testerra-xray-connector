/*
 * Testerra Xray-Connector
 *
 * (C) 2021, Mike Reiche,  T-Systems MMS GmbH, Deutsche Telekom AG
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
 */

package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayNoSync;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.XrayConnector;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.TestType;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraKeyReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatusCategory;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import eu.tsystems.mms.tic.testframework.events.TestStatusUpdateEvent;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.ErrorContext;
import eu.tsystems.mms.tic.testframework.report.model.context.ExecutionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.context.Screenshot;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStep;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStepAction;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import org.apache.commons.lang3.StringUtils;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public abstract class AbstractXrayResultsSynchronizer implements XrayResultsSynchronizer, Loggable, TestStatusUpdateEvent.Listener {
    private static final String VENDOR_PREFIX = "Testerra Xray connector";
    private boolean isSyncEnabled = false;
    private XrayTestExecutionIssue testExecutionIssue;
    private XrayMapper xrayMapper;
    private XrayUtils xrayUtils;
    private final HashMap<String, XrayTestSetIssue> testSetCacheByClassName = new HashMap<>();
    private final HashMap<String, XrayTestIssue> testCacheByMethodName = new HashMap<>();
    private final ConcurrentLinkedQueue<XrayTestExecutionImport.TestRun> testRunSyncQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<XrayTestSetIssue> testSetSyncQueue = new ConcurrentLinkedQueue<>();

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
            log().info(String.format("Using mapper %s", xrayMapper.getClass().getSimpleName()));

            final XrayUtils xrayUtils = getXrayUtils();
            final ExecutionContext executionContext = ExecutionContextController.getCurrentExecutionContext();
            xrayMapper.updateTestExecution(testExecutionIssue, executionContext);

            final Optional<XrayTestExecutionIssue> optionalExistingTestExecution = Optional.ofNullable(xrayMapper.queryTestExecution(testExecutionIssue))
                    .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());

            if (optionalExistingTestExecution.isPresent()) {
                testExecutionIssue = optionalExistingTestExecution.get();
                log().info(String.format("Use existing %s (%s)",
                        IssueType.TestExecution,
                        xrayConfig.getIssueUrl(testExecutionIssue.getKey()).orElse(null)
                ));
                xrayMapper.updateTestExecution(testExecutionIssue, executionContext);
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
        updateTestExecution();
        unregisterSync();
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
        if (realMethod.isAnnotationPresent(XrayNoSync.class) || !realMethod.isAnnotationPresent(Test.class)) {
            return;
        }

        final XrayMapper xrayMapper = getXrayMapper();
        final XrayUtils xrayUtils = getXrayUtils();
        final XrayConfig xrayConfig = getXrayConfig();

        if (!xrayConfig.isSyncEnabled()) {
            log().info("Xray sync is disabled.");
            return;
        }

        // Get the class Test Set issue by annotation
        final Optional<XrayTestSetIssue> optionalXrayTestSetIssue = getTestSetIssueForClassContext(methodContext.getClassContext());

        // Get the method's Test issues by annotation
        final Set<XrayTestIssue> currentTestIssues = getTestIssuesForMethod(realMethod);

        // If no method annotation, but a class annotation was found
        if (currentTestIssues.isEmpty() || optionalXrayTestSetIssue.isPresent()) {
            final String cacheKey = testResult.getMethod().getQualifiedName();

            if (!testCacheByMethodName.containsKey(cacheKey)) {
                final JqlQuery testQuery = xrayMapper.queryTest(methodContext);
                if (testQuery != null) {
                    // Find existing Test issue
                    final Optional<XrayTestIssue> optionalExistingTestIssue = xrayUtils.searchIssues(testQuery, XrayTestIssue::new).findFirst();
                    if (optionalExistingTestIssue.isPresent()) {
                        testCacheByMethodName.put(cacheKey, optionalExistingTestIssue.get());
                    } else if (xrayMapper.shouldCreateNewTest(methodContext)) {
                        // Create new Test issue
                        XrayTestIssue testIssue = new XrayTestIssue();
                        String newKey = String.format("%s.%s", XrayUtils.PREFIX_NEW_ISSUE, xrayMapper.getDefaultTestIssueSummery(methodContext));
                        testIssue.setKey(newKey);
                        testIssue.getProject().setKey(xrayConfig.getProjectKey());
                        testIssue.setSummary(getXrayMapper().getDefaultTestIssueSummery(methodContext));
                        testIssue.setDescription(String.format("%s generated %s by method %s", VENDOR_PREFIX, IssueType.Test, testResult.getMethod().getQualifiedName()));
                        testCacheByMethodName.put(cacheKey, testIssue);
                    }
                }
            }

            // Add the found or new Test issue to the list of current issues
            if (testCacheByMethodName.containsKey(cacheKey)) {
                currentTestIssues.add(testCacheByMethodName.get(cacheKey));
            }
        }

        optionalXrayTestSetIssue.ifPresent(xrayTestSetIssue -> {
            final List<String> testSetTestKeys = xrayTestSetIssue.getTestKeys();
            final List<String> newTestKeys = currentTestIssues.stream()
                    .map(JiraKeyReference::getKey)
                    .filter(Objects::nonNull)
                    .filter(testKey -> !testSetTestKeys.contains(testKey))
                    .collect(Collectors.toList());

            // Add new tests to testset
            if (newTestKeys.size() > 0) {
                xrayMapper.updateTestSet(xrayTestSetIssue, methodContext.getClassContext());
                finalizeTestSet(xrayTestSetIssue, methodContext.getClassContext());
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
        currentTestIssues.stream()
                .peek(issue -> xrayMapper.updateTest(issue, methodContext))
                .map(issue -> this.updateTestInfoForImport(issue, methodContext))
                .peek(testRun -> updateTestRunForImport(testRun, methodContext))
                .forEach(testRunSyncQueue::add);

        if (testRunSyncQueue.size() >= xrayConfig.getSyncFrequencyTests()) {
            flushSyncQueue();
        }
    }

    private synchronized void flushSyncQueue() {
        if (!isSyncEnabled) {
            return;
        }

        final int numTestSetsToSync = testSetSyncQueue.size();
        final int numTestsToSync = testRunSyncQueue.size();

        if (numTestSetsToSync == 0 && numTestsToSync == 0) {
            return;
        }

        log().info("Synchronizing...");

        final XrayUtils xrayUtils = getXrayUtils();

        // Prepare Xray Test execution for import
        XrayTestExecutionImport xrayTestExecutionImport = new XrayTestExecutionImport(getTestExecutionIssue());

        // Add all new tests to execution
        testRunSyncQueue.forEach(test -> {
            xrayTestExecutionImport.addTest(test);
            testRunSyncQueue.remove(test);
        });

        if (this.getExecutionUpdates() != null) {
            log().warn("getExecutionUpdates() is ignored");
        }

        try {
            xrayTestExecutionImport.getInfo().setFinishDate(new Date());
            xrayUtils.importTestExecution(xrayTestExecutionImport);
            this.testExecutionIssue.setKey(xrayTestExecutionImport.getTestExecutionKey());
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

        // xrayTestExecutionImport now contains all keys of created and updated test issues
        //
        // Only run key update on testSetSyncQueue if new tests have to create:
        // New test issues have special key: XrayUtils.PREFIX_NEW_ISSUE + method name
        boolean areNewTestsToImport = testSetSyncQueue.stream().anyMatch(xrayTestSetIssue -> {
            return xrayTestSetIssue.getTestKeys().stream().anyMatch(key -> key.contains(XrayUtils.PREFIX_NEW_ISSUE));
        });
        if (areNewTestsToImport && xrayTestExecutionImport.getResultTestIssueImport() != null) {
            if (xrayTestExecutionImport.getResultTestIssueImport().getError().size() > 0) {
                log().error("Error at syncing with Jira");
                xrayTestExecutionImport.getResultTestIssueImport().getError()
                        .forEach(error -> log().error("{} - {}: {}", error.getProject(), error.getSummary(), error.getMessages().toString()));
                return;
            } else if (xrayTestExecutionImport.getResultTestIssueImport().getSuccess().size() > 0) {
                xrayTestExecutionImport.getResultTestIssueImport().getSuccess().forEach(jiraIssueReference -> {
                    try {
                        // Replace the temporary key with real Jira key from the result 'xrayTestExecutionImport'
                        JiraIssue issue = xrayUtils.getIssue(jiraIssueReference.getKey());
                        testSetSyncQueue.forEach(xrayTestSetIssue -> {
                            for (ListIterator<String> iterator = xrayTestSetIssue.getTestKeys().listIterator(); iterator.hasNext(); ) {
                                String next = iterator.next();
                                if (next.contains(issue.getSummary())) {
                                    iterator.set(issue.getKey());
                                }
                            }
                        });
                    } catch (IOException e) {
                        log().error(String.format("Unable to read %s", IssueType.TestExecution), e);
                    }
                });
            }
        }

        // After fixing temporary key of test issues, the test set can create or update
        testSetSyncQueue.forEach(xrayTestSetIssue -> {
            try {
                xrayUtils.createOrUpdateIssue(xrayTestSetIssue);
                final Optional<URI> issueUrl = getXrayConfig().getIssueUrl(xrayTestSetIssue.getKey());
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
    }

    private synchronized void unregisterSync() {
        if (isSyncEnabled) {
            EventBus eventBus = TesterraListener.getEventBus();
            eventBus.unregister(this);

            isSyncEnabled = false;
        }
    }

    private void updateTestExecution() {
        if (!this.getXrayMapper().shouldUpdateTestExecutionStatus()) {
            return;
        }
        LinkedList<JiraStatusCategory> testExecutionTransitions = this.getXrayMapper().getTestExecutionTransitions();
        if (testExecutionTransitions == null || testExecutionTransitions.size() == 0 || this.testExecutionIssue == null) {
            return;
        }
        final String key = this.testExecutionIssue.getKey();
        this.getXrayUtils().performTransitionChain(key, testExecutionTransitions);
    }

    /**
     * Update the Info object for creating or updating Xray tests.
     * If no Info object is defined, the Xray test will not be updated.
     */
    private XrayTestExecutionImport.TestRun updateTestInfoForImport(XrayTestIssue issue, MethodContext methodContext) {
        XrayTestExecutionImport.TestRun testRun = new XrayTestExecutionImport.TestRun(issue.getKey());
        if (this.getXrayMapper().shouldCreateNewTest(methodContext)) {

            XrayTestExecutionImport.TestRun.Info info = new XrayTestExecutionImport.TestRun.Info();
            info.setDescription(issue.getDescription());
            info.setSummary(issue.getSummary());
            info.setLabels(issue.getLabels());
            info.setDefinition(issue.getSummary());
            info.setType(TestType.AutomatedGeneric);
            info.setProjectKey(issue.getProject().getKey());

            // The test's test type needs to be {@link TestType.Manual} to support test steps.
            info.setType(TestType.Manual);

            List<TestStep> testerraTestSteps = methodContext.readTestSteps().collect(Collectors.toList());
            for (TestStep testerraTestStep : testerraTestSteps) {
                if (testerraTestStep.isInternalTestStep()) {
                    continue;
                }
                /*
                 * The test steps definitions
                 */
                final XrayTestExecutionImport.TestStep importTestStep = new XrayTestExecutionImport.TestStep();
                importTestStep.setAction(testerraTestStep.getName());
                // We always expect the step to pass
                importTestStep.setResult(XrayTestExecutionImport.TestRun.Status.PASS.toString());
                info.addStep(importTestStep);
            }

            testRun.setTestInfo(info);
        }
        return testRun;
    }

    /**
     * Update the TestRun object with result information include test steps
     */
    private void updateTestRunForImport(XrayTestExecutionImport.TestRun testRun, MethodContext methodContext) {
        ITestResult testResult = methodContext.getTestNgResult().get();

        testRun.setStart(new Date(testResult.getStartMillis()));

        switch (testResult.getStatus()) {
            case ITestResult.FAILURE:
                testRun.setStatus(XrayTestExecutionImport.TestRun.Status.FAIL);
                break;
            case ITestResult.SUCCESS:
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                testRun.setStatus(XrayTestExecutionImport.TestRun.Status.PASS);
                break;
            case ITestResult.SKIP:
                testRun.setStart(Calendar.getInstance().getTime());
                testRun.setStatus(XrayTestExecutionImport.TestRun.Status.SKIPPED);
                break;
            default:
                log().error("TestNg result status {} cannot be processed", testResult.getStatus());
        }

        testRun.setFinish(new Date(testResult.getEndMillis()));

        /**
         * Add the results for test steps.
         * The steps will be sync with the order of appearance
         */
        final int lastFailedTestStepIndex = methodContext.getLastFailedTestStepIndex();
        List<TestStep> testerraTestSteps = methodContext.readTestSteps().collect(Collectors.toList());
        int stepIndex = -1;
        for (TestStep testerraTestStep : testerraTestSteps) {
            ++stepIndex;

            if (testerraTestStep.isInternalTestStep()) {
                continue;
            }

            /*
             * The actual Test Run step
             */
            XrayTestExecutionImport.TestRun.Status actualStatus = XrayTestExecutionImport.TestRun.Status.PASS;

            if (stepIndex == lastFailedTestStepIndex) {
                actualStatus = XrayTestExecutionImport.TestRun.Status.FAIL;
            }
            final XrayTestExecutionImport.TestRun.Step testRunStep = new XrayTestExecutionImport.TestRun.Step();
            testRunStep.setStatus(actualStatus);
            testRun.addStep(testRunStep);

            testerraTestStep.readActions()
                    .flatMap(TestStepAction::readEntries)
                    .forEach(entry -> {
                        if (entry instanceof ErrorContext) {
                            ErrorContext errorContext = (ErrorContext) entry;
                            testRunStep.setActualResult(errorContext.getThrowable().getMessage());
                        } else if (entry instanceof Screenshot) {
                            Screenshot screenshot = (Screenshot) entry;
                            try {
                                XrayTestExecutionImport.TestRun.Evidence evidence = new XrayTestExecutionImport.TestRun.Evidence(screenshot.getScreenshotFile());
                                testRunStep.addEvidence(evidence);
                            } catch (IOException e) {
                                log().error("Unable to add evidence screenshot", e);
                            }
                            screenshot.getPageSourceFile().ifPresent(file -> {
                                try {
                                    XrayTestExecutionImport.TestRun.Evidence evidence = new XrayTestExecutionImport.TestRun.Evidence(file);
                                    testRunStep.addEvidence(evidence);
                                } catch (IOException e) {
                                    log().error("Unable to add evidence page source file", e);
                                }
                            });
                        }
                    });
        }
    }

    private Set<XrayTestIssue> getTestIssuesForMethod(Method realMethod) {
        if (!realMethod.isAnnotationPresent(XrayTest.class)) {
            return new HashSet<>();
        }

        String[] testKeys = realMethod.getAnnotation(XrayTest.class).key();
        return Arrays.stream(testKeys)
                .filter(StringUtils::isNotBlank)
                .map(XrayTestIssue::new)
                .collect(Collectors.toSet());
    }

    /**
     * Queries Test Sets from API, creates new ones and caches them locally.
     * If no Test Set could be found or wasn't created, the returned Optional is empty.
     */
    private Optional<XrayTestSetIssue> getTestSetIssueForClassContext(final ClassContext classContext) {
        final Class<?> clazz = classContext.getTestClass();

        if (!clazz.isAnnotationPresent(XrayTestSet.class)) {
            return Optional.empty();
        }

        final String cacheKey = clazz.getCanonicalName();

        // The Test Set has been cached by name, even if its null
        if (this.testSetCacheByClassName.containsKey(cacheKey)) {
            return Optional.ofNullable(this.testSetCacheByClassName.get(cacheKey));
        }

        XrayTestSetIssue xrayTestSetIssue = null;
        final XrayMapper xrayMapper = getXrayMapper();
        final XrayUtils xrayUtils = getXrayUtils();
        final XrayConfig xrayConfig = getXrayConfig();

        // Test set key is present
        final String testSetKey = clazz.getAnnotation(XrayTestSet.class).key();
        if (StringUtils.isNotBlank(testSetKey)) {
            try {
                final XrayTestSetIssue existingTestSetIssue = xrayUtils.getIssue(testSetKey, XrayTestSetIssue::new);
                xrayTestSetIssue = new XrayTestSetIssue(existingTestSetIssue);
            } catch (IOException e) {
                log().error(String.format("Unable to query %s by key: %s", IssueType.TestSet, testSetKey), e);
            }
        } else {
            final JqlQuery testSetQuery = xrayMapper.queryTestSet(classContext);
            if (testSetQuery != null) {
                final Optional<XrayTestSetIssue> optionalExistingTestSetIssue = xrayUtils.searchIssues(testSetQuery, XrayTestSetIssue::new).findFirst();
                if (optionalExistingTestSetIssue.isPresent()) {
                    xrayTestSetIssue = new XrayTestSetIssue(optionalExistingTestSetIssue.get());
                } else if (xrayMapper.shouldCreateNewTestSet(classContext)) {
                    xrayTestSetIssue = new XrayTestSetIssue();
                    xrayTestSetIssue.getProject().setKey(xrayConfig.getProjectKey());
                }
            }
        }

        // cache could be null
        this.testSetCacheByClassName.put(cacheKey, xrayTestSetIssue);
        return Optional.ofNullable(xrayTestSetIssue);
    }

    private void finalizeTestSet(XrayTestSetIssue xrayTestSetIssue, ClassContext classContext) {
        final Class<?> clazz = classContext.getTestClass();
        if (StringUtils.isBlank(xrayTestSetIssue.getSummary())) {
            xrayTestSetIssue.setSummary(clazz.getSimpleName());
        }

        if (StringUtils.isBlank(xrayTestSetIssue.getDescription())) {
            xrayTestSetIssue.setDescription(String.format("%s generated %s by class %s", VENDOR_PREFIX, IssueType.TestSet, clazz.getCanonicalName()));
        }
    }
}
