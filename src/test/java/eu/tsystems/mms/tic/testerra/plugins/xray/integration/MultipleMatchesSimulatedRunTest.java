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

import com.google.common.collect.ImmutableList;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation.ClassAnnotatedWithKeyAndMultipleMethodMatchesTest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Calendar;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class MultipleMatchesSimulatedRunTest extends AbstractSimulatedRunTest {

    @BeforeMethod
    protected void setInvalidTestStatus(Method method) throws IOException {
        super.setInvalidTestStatus(eu.tsystems.mms.tic.testerra.plugins.xray.TestData.TEST_EXEC_KEY_MULTIPLE);
    }

    @Override
    protected String getExpectedSummary() {
        return "Simulated Multiple Matches Execution";
    }

    @Override
    protected String getExpectedDescription() {
        return "simulated test run";
    }

    @Override
    protected String getExpectedRevision() {
        return eu.tsystems.mms.tic.testerra.plugins.xray.TestData.TEST_EXEC_REVISION_MULTIPLE;
    }

    @Test
    public void testMultipleTestsMatchQuery() throws IOException, ParseException {
        final Calendar before = Calendar.getInstance();
        final TestNG testNG = new TestNG();
        testNG.setTestClasses(new Class[] {ClassAnnotatedWithKeyAndMultipleMethodMatchesTest.class});
        testNG.setListenerClasses(ImmutableList.of(FakeTesterraEventTriggerer.class));
        testNG.setParallel(XmlSuite.ParallelMode.METHODS);
        testNG.run();
        checkTestExecutionResult(eu.tsystems.mms.tic.testerra.plugins.xray.TestData.TEST_EXEC_KEY_MULTIPLE, before);
    }
}
