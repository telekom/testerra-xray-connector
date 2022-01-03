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

import com.google.common.collect.Sets;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.NotSyncableException;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayMapper;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayTestExecutionUpdates;
import eu.tsystems.mms.tic.testframework.events.MethodEndEvent;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import java.io.IOException;
import java.util.Set;
import org.testng.ITestResult;

public class PostHocSyncStrategy extends AbstractSyncStrategy {

    private XrayTestExecutionImport testExecution;
    private final Set<XrayTestExecutionImport.Test> tests = Sets.newConcurrentHashSet();
    private String testExecKey = null;


    public PostHocSyncStrategy(XrayInfo xrayInfo, XrayMapper xrayMapper, XrayTestExecutionUpdates updates) {
        super(xrayInfo, xrayMapper, updates);
    }

    @Override
    public void onStart() {
        try {
            final Set<String> foundTestExecutionKeys = connector.searchForExistingTestExecutions();
            if (foundTestExecutionKeys.size() == 1) {
                /* one hit */
                testExecKey = foundTestExecutionKeys.iterator().next();
                testExecution = new XrayTestExecutionImport(testExecKey);
                connector.updateStartTimeOfTestExecution(testExecKey);
            } else if (foundTestExecutionKeys.isEmpty()) {
                /* no hit */
                testExecution = new XrayTestExecutionImport(xrayInfo);
            } else {
                final String message = "must find at maximum one match for jql query";
                ExecutionContextController.getCurrentMethodContext().addPriorityMessage("Synchronization to Xray failed: " + message);
                throw new NotSyncableException(message);
            }
        } catch (IOException e) {
            log().error(e.getMessage());
        }

    }

    @Override
    public void onFinish() {
        try {
            testExecution.setTests(tests);
            if (testExecKey == null) {
                /* new execution */
                testExecKey = connector.syncTestExecutionReturnKey(testExecution);
                final JiraIssueUpdate onNewExecutionCreated = updates.updateOnNewExecutionCreated();
                if (onNewExecutionCreated != null) {
                    connector.updateIssue(testExecKey, onNewExecutionCreated);
                }
                connector.doTransitions(testExecKey, XrayConfig.getInstance().getTransitionsOnCreated());
            } else {
                /* existing execution */
                connector.syncTestExecutionReturnKey(testExecution);
                final JiraIssueUpdate onExistingExecutionUpdated = updates.updateOnExistingExecutionUpdated();
                if (onExistingExecutionUpdated != null) {
                    connector.updateIssue(testExecKey, onExistingExecutionUpdated);
                }
                connector.doTransitions(testExecKey, XrayConfig.getInstance().getTransitionsOnUpdated());
            }
            uploadAttachments(testExecKey);
            uploadComments(testExecKey);

            /* execution done */
            final JiraIssueUpdate onExecutionDone = updates.updateOnExecutionDone();
            if (onExecutionDone != null) {
                connector.updateIssue(testExecKey, onExecutionDone);
            }
            connector.doTransitions(testExecKey, XrayConfig.getInstance().getTransitionsOnDone());
            connector.updateFinishTimeOfTestExecution(testExecKey);
        } catch (IOException e) {
            log().error(e.getMessage());
        }
    }

    @Override
    public void onTestSuccess(MethodEndEvent testResult) {
        finishTest(testResult);
    }

    @Override
    public void onTestFailure(MethodEndEvent testResult) {
        finishTest(testResult);
    }

    @Override
    public void onTestSkip(MethodEndEvent testResult) {
        finishTest(testResult);
    }

    private void finishTest(MethodEndEvent event) {
        final ITestResult result = event.getTestResult();
        getTestKeysFromAnnotation(event).ifPresent(testKeys -> {
            log().info("Synchronize: " + String.join(", ", testKeys));
            for (final String testKey : testKeys) {
                final XrayTestExecutionImport.Test xrayTestIssue = createXrayTestIssue(testKey, result);
                tests.add(xrayTestIssue);
            }
        });
    }
}
