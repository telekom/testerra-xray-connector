/*
 * Testerra Xray-Connector
 *
 * (C) 2022, Martin Großmann,  T-Systems MMS GmbH, Deutsche Telekom AG
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
package eu.tsystems.mms.tic.testerra.plugins.xray.tests.integration;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayNoSync;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.demotests.ClassAnnotatedTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.demotests.MethodsAnnotatedTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.demotests.MethodsAnnotatedWithStepsTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.tests.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.synchronizer.EmptyMapperResultsSynchronizer;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.synchronizer.SummaryMapperResultsSynchronizer;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2022-10-13
 *
 * @author mgn
 */
public class SynchronizationTests extends AbstractTest {

    private XrayUtils xrayUtils;

    @BeforeClass
    public void prepareWebResource() throws URISyntaxException {
        super.prepareWebResource();
        xrayUtils = new XrayUtils(webResource);
    }

    @DataProvider
    private Iterator<TestContainer> dataProviderSyncTests() {
        List<TestContainer> containerList = new ArrayList<>();

        // Sync with EmptyMapper, annotated tests and without steps
        TestContainer container = new TestContainer(
                EmptyMapperResultsSynchronizer.EXECUTION_SUMMARY1,
                MethodsAnnotatedTest.getExpectedTestRuns()
        );
        containerList.add(container);

        // Sync with EmptyMapper, annotated tests and with steps
        container = new TestContainer(
                EmptyMapperResultsSynchronizer.EXECUTION_SUMMERY2,
                MethodsAnnotatedWithStepsTest.getExpectedTestRuns()
        );
        containerList.add(container);

        // Sync with DefaultSummaryMapper, annotated class with key and without steps
        container = new TestContainer(
                SummaryMapperResultsSynchronizer.EXECUTION_SUMMARY1,
                ClassAnnotatedTest.getExpectedTestRuns()
        );
        containerList.add(container);

        // Sync with DefaultSummaryMapper, annotated class without key and without steps
        container = new TestContainer(
                SummaryMapperResultsSynchronizer.EXECUTION_SUMMARY2,
                ClassAnnotatedTest.getExpectedTestRuns()
        );
        containerList.add(container);

        return containerList.iterator();
    }

    @XrayNoSync
    @Test(dataProvider = "dataProviderSyncTests")
    public void testXraySynchronizationResults(TestContainer container) throws IOException {
        // Get latest Test execution based on summary
        Optional<XrayTestExecutionIssue> existingTestExecution = xrayUtils
                .searchIssues(getTestExecutionJqlQuery(container.executionSummary), XrayTestExecutionIssue::new)
                .min((elem1, elem2) -> elem2.getKey().compareTo(elem1.getKey()));
        Assert.assertTrue(existingTestExecution.isPresent());

        // Check expected test runs with test runs from execution
        Set<XrayTestExecutionImport.TestRun> executionTestRuns = xrayUtils.getTestRunsByTestExecutionKey(existingTestExecution.get().getKey());
        executionTestRuns.forEach(testRun -> {
            XrayTestExecutionImport.TestRun expectedRun = container.expectedRuns.get(testRun.getTestKey());
            Assert.assertNotNull(expectedRun);

            XrayTestIssue testIssue = getXrayTestIssueByKey(xrayUtils, testRun.getTestKey());
            Assert.assertNotNull(testIssue);

            Assert.assertEquals(testRun.getStatus(), expectedRun.getStatus());
            Assert.assertEquals(testIssue.getSummary(), expectedRun.getTestInfo().getSummary());

            // Assert the step results, step order of test run should be the same step order in expected run result
            if (expectedRun.getSteps() != null && expectedRun.getSteps().size() > 0) {
                Assert.assertEquals(testRun.getSteps().size(), expectedRun.getSteps().size(), "Step size of both test runs should equal");
                AtomicInteger i = new AtomicInteger(0);
                expectedRun.getSteps().forEach(step -> {
                    Assert.assertEquals(step.getStatus(), testRun.getSteps().get(i.getAndIncrement()).getStatus());
                });
            }
        });
    }

    public class TestContainer {

        String executionSummary;
        Map<String, XrayTestExecutionImport.TestRun> expectedRuns;

        public TestContainer(String summary, Map<String, XrayTestExecutionImport.TestRun> expectedRuns) {
            this.executionSummary = summary;
            this.expectedRuns = expectedRuns;
        }
    }

}
