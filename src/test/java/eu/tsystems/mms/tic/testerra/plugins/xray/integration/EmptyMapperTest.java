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
package eu.tsystems.mms.tic.testerra.plugins.xray.integration;

import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayNoSync;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.hook.XrayConnectorHook;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation.MethodsAnnotatedTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.synchronizer.EmptyMapperResultsSynchronizer;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created on 2022-10-13
 *
 * @author mgn
 */
public class EmptyMapperTest extends AbstractTest {

    private XrayUtils xrayUtils;

    private List<XrayTestExecutionImport.TestRun> verifyTestRuns;

    @BeforeClass
    public void prepareWebResource() throws URISyntaxException {
        super.prepareWebResource();
        xrayUtils = new XrayUtils(webResource);
        verifyTestRuns = MethodsAnnotatedTest.getTestRunsForVerification();
        XrayConnectorHook.getInstance().setXrayResultsSynchronizer(null);
    }

    @XrayNoSync
    @Test
    public void testEmptyMapperExecution() throws IOException {
        // Get Test execution based on summary

        Optional<XrayTestExecutionIssue> existingTestExecution = xrayUtils
                .searchIssues(getTestExecutionJqlQuery(EmptyMapperResultsSynchronizer.EXECUTION_SUMMARY), XrayTestExecutionIssue::new)
                .min((elem1, elem2) -> elem2.getKey().compareTo(elem1.getKey()));
        Assert.assertTrue(existingTestExecution.isPresent());

        // Check every test run
        Set<XrayTestExecutionImport.TestRun> executionTestRuns = xrayUtils.getTestRunsByTestExecutionKey(existingTestExecution.get().getKey());
        executionTestRuns.forEach(testRun -> {
            Optional<XrayTestExecutionImport.TestRun> foundTestRun = verifyTestRuns.stream().filter(run -> run.getTestKey().equals(testRun.getTestKey())).findFirst();
            Assert.assertTrue(foundTestRun.isPresent());

            XrayTestIssue testIssue = getXrayTestIssueByKey(xrayUtils, testRun.getTestKey());
            Assert.assertNotNull(testIssue);

            Assert.assertEquals(testRun.getStatus(), foundTestRun.get().getStatus());
            Assert.assertEquals(testIssue.getSummary(), foundTestRun.get().getTestInfo().getSummary());
            Assert.assertEquals(testIssue.getDescription(), foundTestRun.get().getTestInfo().getDescription());
        });
    }


}
