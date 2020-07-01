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

package eu.tsystems.mms.tic.testerra.plugins.xray.integration;

import eu.tsystems.mms.tic.testerra.plugins.xray.hook.XrayConnectorHook;
import eu.tsystems.mms.tic.testframework.events.TesterraEvent;
import eu.tsystems.mms.tic.testframework.events.TesterraEventDataType;
import eu.tsystems.mms.tic.testframework.events.TesterraEventService;
import eu.tsystems.mms.tic.testframework.events.TesterraEventType;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;


/**
 * Created by maco on 19.05.2017.
 * <p>
 * replacement for TesterraListener which may not be instantiated more thane once per execution. Fires necesarry events
 * to acturally trigger behavior in {@link eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.AbstractXrayResultsSynchronizer}
 */
public class FakeTesterraEventTriggerer implements ITestListener, IInvokedMethodListener, ISuiteListener, IReporter {

    private final XrayConnectorHook xrayConnectorHook = new XrayConnectorHook();


    @Override
    public void onTestStart(ITestResult result) {

    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TesterraEventService.getInstance().fireEvent(new TesterraEvent(TesterraEventType.SYNC_METHOD_RESULT)
                .addData(TesterraEventDataType.ITestResult, result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TesterraEventService.getInstance().fireEvent(new TesterraEvent(TesterraEventType.SYNC_METHOD_RESULT)
                .addData(TesterraEventDataType.ITestResult, result));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TesterraEventService.getInstance().fireEvent(new TesterraEvent(TesterraEventType.SYNC_METHOD_RESULT)
                .addData(TesterraEventDataType.ITestResult, result));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {


    }

    @Override
    public void onFinish(ITestContext context) {

    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

        if (method.isTestMethod()) {
            final MethodContext methodContext = ExecutionContextController
                    .setCurrentTestResult(testResult, testResult.getTestContext()); // stores the actual testresult, auto-creates the method context
            ExecutionContextController.setCurrentMethodContext(methodContext);
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            ExecutionContextController.clearCurrentTestResult();
        }
    }

    @Override
    public void onStart(ISuite suite) {

        xrayConnectorHook.init();
    }

    @Override
    public void onFinish(ISuite suite) {

    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        xrayConnectorHook.terminate();
    }
}
