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

import static eu.tsystems.mms.tic.testerra.plugins.xray.TestData.TEST_EXEC_KEY_DEFAULT;


import com.google.common.collect.ImmutableList;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation.ClassAnnotatedWithKeyTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation.ClassAnnotatedWithoutKeyTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation.MethodsAnnotatedTest;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Calendar;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

@Listeners(TesterraListener.class)
public class AnnotationsSimulatedRunTest extends SimulatedRunTest {

    @BeforeMethod(alwaysRun = true)
    protected void setInvalidTestStatus(Method method) throws IOException {
        super.setInvalidTestStatus(TEST_EXEC_KEY_DEFAULT);
    }

    @Override
    protected String getExpectedSummary() {
        return "Simulated Run Execution";
    }

    @Override
    protected String getExpectedDescription() {
        return "simulated test run";
    }

    @Override
    protected String getExpectedRevision() {
        return eu.tsystems.mms.tic.testerra.plugins.xray.TestData.TEST_EXEC_REVISION_DEFAULT;
    }

    @Test
    public void testMethodsAnnotated() throws IOException, ParseException {

        final Calendar before = Calendar.getInstance();
        final TestNG testNG = new TestNG();
        testNG.setTestClasses(new Class[] {MethodsAnnotatedTest.class});
        testNG.setListenerClasses(ImmutableList.of(FakeTesterraEventTriggerer.class));
        testNG.setParallel(XmlSuite.ParallelMode.METHODS);
        testNG.run();
        removeListenersFromTesterraEventService(testNG);
        checkTestExecutionResult(TEST_EXEC_KEY_DEFAULT, fullWithoutParametrized, before);
    }

    @Test
    public void testClassAnnotatedWithKey() throws IOException, ParseException {

        final Calendar before = Calendar.getInstance();
        final TestNG testNG = new TestNG();
        testNG.setTestClasses(new Class[] {ClassAnnotatedWithKeyTest.class});
        testNG.setListenerClasses(ImmutableList.of(FakeTesterraEventTriggerer.class));
        testNG.setParallel(XmlSuite.ParallelMode.METHODS);
        testNG.run();
        removeListenersFromTesterraEventService(testNG);

        checkTestExecutionResult(TEST_EXEC_KEY_DEFAULT, before);
    }

    @Test
    public void testClassAnnotatedWithoutKey() throws IOException, ParseException {

        final Calendar before = Calendar.getInstance();
        final TestNG testNG = new TestNG();
        testNG.setTestClasses(new Class[] {ClassAnnotatedWithoutKeyTest.class});
        testNG.setListenerClasses(ImmutableList.of(FakeTesterraEventTriggerer.class));
        testNG.setParallel(XmlSuite.ParallelMode.METHODS);
        testNG.run();
        removeListenersFromTesterraEventService(testNG);
        checkTestExecutionResult(TEST_EXEC_KEY_DEFAULT, before);
    }

}
