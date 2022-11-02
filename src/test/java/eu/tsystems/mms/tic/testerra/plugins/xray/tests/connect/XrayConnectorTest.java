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

package eu.tsystems.mms.tic.testerra.plugins.xray.tests.connect;

import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.tests.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.DefaultSummaryMapper;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.Optional;

public class XrayConnectorTest extends AbstractTest {

    private String projectKey = "SWFTE";
    private final String summary = "Dummy Execution";
    private final String description = "dummy description";
    private WebResource webResource;
    private String revision = "2017-04-10_dummy";
    private XrayTestExecutionIssue testExecutionIssue;
    private XrayTestExecutionIssue testExecutionIssueWithUmlauts;
    private XrayUtils xrayUtils;
    private final DefaultSummaryMapper defaultSummaryMapper = new DefaultSummaryMapper();

    @BeforeTest
    public void prepareWebResource() throws URISyntaxException {
//        webResource = eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils.prepareWebResource("sync.test.properties");
        webResource = eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils.prepareWebResource("xray.properties");
        xrayUtils = new XrayUtils(webResource);
        testExecutionIssue = generateNewTestExecution(projectKey, summary, description, revision);
        testExecutionIssueWithUmlauts = generateNewTestExecution(projectKey, "Dummy Umlaut Execution äöüßÄÖÜ", "dummy description mit Umlauten äöüßÄÖÜ", revision);
    }

    @Test
    public void testSearchForExistingTestExecution() {
        Optional<XrayTestExecutionIssue> optionalExistingTestExecution =
                Optional.ofNullable(defaultSummaryMapper.queryTestExecution(testExecutionIssue))
                        .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());

        Assert.assertTrue(optionalExistingTestExecution.isPresent());
        Assert.assertEquals(optionalExistingTestExecution.get().getKey(), "SWFTE-799");
    }

    @Test
    public void testSearchForExistingTestExecutionContainingUmlauts() {
        Optional<XrayTestExecutionIssue> optionalExistingTestExecution =
                Optional.ofNullable(defaultSummaryMapper.queryTestExecution(testExecutionIssueWithUmlauts))
                        .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());

        Assert.assertTrue(optionalExistingTestExecution.isPresent());
        Assert.assertEquals(optionalExistingTestExecution.get().getKey(), "SWFTE-798");
    }

    @Test
    public void testSearchForNonExistingTestExecution() {
        XrayTestExecutionIssue nonExisitngExecution = testExecutionIssue;
        nonExisitngExecution.setRevision("not existing");
        Optional<XrayTestExecutionIssue> optionalExistingTestExecution =
                Optional.ofNullable(defaultSummaryMapper.queryTestExecution(nonExisitngExecution))
                        .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());
        Assert.assertFalse(optionalExistingTestExecution.isPresent());
    }

    @Test
    public void testSearchForExistingTestIssue() {
        MethodContext methodContext = new MethodContext("Dummy Test Issue", MethodContext.Type.TEST_METHOD, null);
        Optional<XrayTestIssue> testIssue =
                Optional.ofNullable(defaultSummaryMapper.queryTest(methodContext))
                        .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestIssue::new).findFirst());
        Assert.assertTrue(testIssue.isPresent());
        Assert.assertEquals(testIssue.get().getKey(), "SWFTE-1391");
    }

    //
    //    @Test
    //    public void testAddTestExecutionComment() throws Exception {
    //        final XrayConnector xrayConnector = new XrayConnector(summary, description, Collections.singletonList("6.5.0"), "RC6.5.0_00001");
    //        final String issueKey = "SOLI-13149";
    //        final String randomString = RandomUtils.generateRandomString();
    //        xrayConnector.addTestExecutionComment(issueKey, randomString);
    //        final JiraIssue issue = JiraUtils.getIssue(webResource, issueKey);
    //        final JsonNode comments = issue.getFields().findValue("comments");
    //        boolean commentFound = false;
    //        for (final JsonNode comment : comments) {
    //            final String commentText = comment.findValue("body").asText();
    //            if (commentText.equals(randomString)) {
    //                commentFound = true;
    //            }
    //        }
    //        Assert.assertTrue(commentFound, "random comment found");
    //
    //    }
}
