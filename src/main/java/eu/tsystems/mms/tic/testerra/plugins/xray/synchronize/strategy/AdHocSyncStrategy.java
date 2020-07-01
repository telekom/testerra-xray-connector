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
import com.google.common.collect.Sets;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.ExistingXrayTestExecution;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.NotSyncableException;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayMapper;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayTestExecutionUpdates;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import java.io.IOException;
import org.testng.ITestResult;


public class AdHocSyncStrategy extends SyncStrategy {

    private String testExecKey = null;

    public AdHocSyncStrategy(XrayInfo xrayInfo, XrayMapper xrayMapper, XrayTestExecutionUpdates updates) {
        super(xrayInfo, xrayMapper, updates);
    }

    @Override
    public void onStart() {
        try {
            testExecKey = connector.findOrCreateTestExecution(updates.updateOnNewExecutionCreated(), updates.updateOnExistingExecutionUpdated());
        } catch (final IOException e) {
            ExecutionContextController.getCurrentMethodContext().addPriorityMessage("Synchronization to X-ray failed.");
            throw new NotSyncableException(e);
        }
        connector.updateStartTimeOfTestExecution(testExecKey);
    }

    @Override
    public void onFinish() {
        uploadAttachments(testExecKey);
        uploadComments(testExecKey);
        final JiraIssueUpdate onExecutionDone = updates.updateOnExecutionDone();
        if (onExecutionDone != null) {
            try {
                connector.updateIssue(testExecKey, onExecutionDone);
            } catch (final JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        }
        connector.doTransitions(testExecKey, XrayConfig.getInstance().getTransitionsOnDone());
    }


    @Override
    public void onTestSuccess(ITestResult testResult) {
        finishTest(testResult);
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        finishTest(testResult);
    }

    @Override
    public void onTestSkip(ITestResult testResult) {
        finishTest(testResult);
    }


    private void finishTest(ITestResult result) {

        final String testKey = getTestKeyOrHandle(result);

        if (testKey != null) {

            final XrayTestIssue xrayTestIssue = createXrayTestIssue(testKey, result);

            final ExistingXrayTestExecution testExecution = new ExistingXrayTestExecution(testExecKey);
            testExecution.setTests(Sets.newHashSet(xrayTestIssue));

            try {
                connector.syncTestExecutionReturnKey(testExecution);
            } catch (final IOException e) {
                logger.error(e.getMessage());
            }
            connector.updateFinishTimeOfTestExecution(testExecKey);
        }
    }

}
