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

package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayNoSync;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.XrayConnector;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestStatus;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.NotSyncableException;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.TestExecutionAttachment;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayMapper;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayTestExecutionUpdates;
import eu.tsystems.mms.tic.testframework.events.MethodEndEvent;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;


public abstract class SyncStrategy {

    protected final XrayConnector connector;
    protected final XrayTestExecutionUpdates updates;
    protected final XrayInfo xrayInfo;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final XrayMapper xrayMapper;
    private final BlockingQueue<TestExecutionAttachment> attachmentQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> commentQueue = new LinkedBlockingQueue<>();

    public SyncStrategy(XrayInfo xrayInfo, XrayMapper xrayMapper, XrayTestExecutionUpdates updates) {
        this.xrayInfo = xrayInfo;
        this.xrayMapper = xrayMapper;
        this.updates = updates;
        connector = new XrayConnector(xrayInfo);
    }

    private String[] getTestKeysFromAnnotation(final MethodEndEvent event) throws NotSyncableException {

        final ITestResult testResult = event.getTestResult();
        if (isNoSyncDesired(testResult.getMethod())) {
            throw new NotSyncableException("no sync expected", true);
        }

        final String[] methodResult = readFromMethodAnnotation(testResult.getMethod());
        if (methodResult != null) {
            saveKeyFromXrayTestAnnotationToReport(event);
            return methodResult;
        }

        final String testSetResult = readFromClassAnnotation(event);
        if (testSetResult != null) {
            return new String[] {testSetResult};
        }

        throw new NotSyncableException(String.format("no test key retrieved for method %s", resultToQualifiedMethodName(testResult)));
    }

    private String readFromClassAnnotation(final MethodEndEvent event) {
        final ITestResult testResult = event.getTestResult();
        final Class<?> clazz = testResult.getMethod().getTestClass().getRealClass();

        if (clazz.isAnnotationPresent(XrayTestSet.class)) {
            final JqlQuery classReferenceQuery = xrayMapper.classToXrayTestSet(testResult.getMethod().getTestClass());
            final JqlQuery resultToReferenceQuery = xrayMapper.resultToXrayTest(testResult);

            String testSetKey = null;
            final String annotatedTestSetKey = clazz.getAnnotation(XrayTestSet.class).key();

            /* retrieve test set key */
            if (!annotatedTestSetKey.isEmpty()) {
                testSetKey = annotatedTestSetKey;
            } else if (classReferenceQuery != null) {
                final Collection<String> foundTestSetKeys = connector.findTestSetKeys(classReferenceQuery);
                testSetKey = getOneMatchOrNull(foundTestSetKeys);
            }

            if (testSetKey != null && resultToReferenceQuery != null) {
                /* find test case from test method using methodToReferenceQuery */
                final Collection<String> testKeys = connector.findTestKeys(testSetKey, resultToReferenceQuery);
                final String oneMatchOrNull = getOneMatchOrNull(testKeys);

                if (oneMatchOrNull != null) {
                    saveTicketIdForReport(event, oneMatchOrNull);
                }
                return oneMatchOrNull;
            }
            logger.warn("no test key could be retrieved for method {} within test set {}, no sync of test result",
                    resultToQualifiedMethodName(testResult), testSetKey);
        }
        return null;
    }

    private void saveKeyFromXrayTestAnnotationToReport(final MethodEndEvent event) {

        final Method javaMethod = event.getTestResult().getMethod().getConstructorOrMethod().getMethod();

        if (javaMethod.isAnnotationPresent(XrayTest.class)) {
            final XrayTest annotation = javaMethod.getAnnotation(XrayTest.class);
            for (final String ticketId : annotation.key()) {
                saveTicketIdForReport(event, ticketId);
            }
        }
    }

    private void saveTicketIdForReport(final MethodEndEvent event, final String ticketId) {

        final XrayConfig instance = XrayConfig.getInstance();
        final URI restServiceUri = instance.getRestServiceUri();

        final String jiraBaseUri = restServiceUri.toString().replace(restServiceUri.getPath(), "/browse");

        if (!ticketId.isEmpty()) {
            final String ticketUriString = jiraBaseUri.concat("/" + ticketId);
            final String ticketUriHtml = "<a href= '" + ticketUriString + "'>Xray Ticket: " + ticketId + "</a>";

            final MethodContext currentMethodContext = event.getMethodContext();
            currentMethodContext.infos.add(ticketUriHtml);
        }
    }

    private String resultToQualifiedMethodName(ITestResult testResult) {
        final Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }

    private boolean isNoSyncDesired(final ITestNGMethod testNGMethod) {
        final Method javaMethod = testNGMethod.getConstructorOrMethod().getMethod();
        final Class<?> clazz = testNGMethod.getTestClass().getRealClass();
        return clazz.isAnnotationPresent(XrayNoSync.class) || javaMethod.isAnnotationPresent(XrayNoSync.class);
    }

    private String[] readFromMethodAnnotation(final ITestNGMethod testNGMethod) {
        final Method javaMethod = testNGMethod.getConstructorOrMethod().getMethod();
        if (javaMethod.isAnnotationPresent(XrayTest.class)) {
            return javaMethod.getAnnotation(XrayTest.class).key();
        }
        return null;
    }

    private <T> T getOneMatchOrNull(final Collection<T> collection) throws NotSyncableException {
        if (collection.isEmpty()) {
            logger.error("collection is empty, returning null");
            return null;
        } else if (collection.size() == 1) {
            return collection.iterator().next();
        } else {
            logger.error("more than one item in collection: {}, returning null", collection);
            return null;
        }
    }

    public void addTestExecutionAttachment(InputStream is, String fileName) {
        attachmentQueue.add(new TestExecutionAttachment(is, fileName));
    }

    public void addTestExecutionComment(String comment) {
        commentQueue.add(comment);
    }

    protected void uploadAttachments(String testExecutionKey) {
        for (final TestExecutionAttachment attachment : attachmentQueue) {
            connector.uploadTestExecutionAttachment(testExecutionKey, attachment.getInputStream(), attachment.getFileName());
        }
    }

    protected void uploadComments(String testExecutionKey) {
        for (final String comment : commentQueue) {
            try {
                connector.addTestExecutionComment(testExecutionKey, comment);
            } catch (JsonProcessingException e) {
                logger.error("adding test execution comment threw exception", e);
            }
        }
    }

    protected XrayTestIssue createXrayTestIssue(String testKey, ITestResult result) {
        final XrayTestIssue xrayTestIssue = new XrayTestIssue();
        xrayTestIssue.setTestKey(testKey);
        xrayTestIssue.setStart(new Date(result.getStartMillis()));

        switch (result.getStatus()) {
            case ITestResult.FAILURE:
                xrayTestIssue.setStatus(XrayTestStatus.FAIL);
                break;
            case ITestResult.SUCCESS:
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                xrayTestIssue.setStatus(XrayTestStatus.PASS);
                break;
            case ITestResult.SKIP:
                xrayTestIssue.setStart(Calendar.getInstance().getTime());
                xrayTestIssue.setStatus(XrayTestStatus.SKIPPED);
                break;
            default:
                logger.error("test-ng result status {} cannot be processed", result.getStatus());
        }

        xrayTestIssue.setFinish(new Date(result.getEndMillis()));
        return xrayTestIssue;
    }

    protected String[] getTestKeys(MethodEndEvent result) {
        String[] testKeys = null;
        try {
            testKeys = getTestKeysFromAnnotation(result);
        } catch (NotSyncableException e) {
            if (!e.isExpected()) {
                logger.error(e.getMessage());
                if (ExecutionContextController.getCurrentMethodContext() != null) {
                    ExecutionContextController.getCurrentMethodContext().addPriorityMessage("Synchronization to Xray failed.");
                }
            } else {
                logger.info(XrayNoSync.class.getSimpleName() + " annotation found, no sync of test method is desired");
            }
        }
        return testKeys;
    }

    public abstract void onTestSuccess(MethodEndEvent testResult);

    public abstract void onTestFailure(MethodEndEvent testResult);

    public abstract void onTestSkip(MethodEndEvent testResult);

    public abstract void onStart();

    public abstract void onFinish();
}

