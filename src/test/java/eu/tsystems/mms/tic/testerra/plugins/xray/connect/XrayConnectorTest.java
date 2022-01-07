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

package eu.tsystems.mms.tic.testerra.plugins.xray.connect;


import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.DefaultSummaryMapper;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import java.net.URISyntaxException;
import java.util.Optional;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class XrayConnectorTest extends AbstractTest {

    private final String summary = "Dummy Execution";
    private final String description = "dummy description";
    private WebResource webResource;
    private String revision = "2017-04-10_dummy";
    private XrayInfo xrayInfo;
    private XrayInfo xrayInfoWithUmlauts;
    private XrayUtils xrayUtils;
    private final DefaultSummaryMapper defaultSummaryMapper = new DefaultSummaryMapper();

    @BeforeTest
    public void prepareWebResource() throws URISyntaxException {
        webResource = eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils.prepareWebResource("sync.test.properties");
        xrayUtils = new XrayUtils(webResource);
        xrayInfo = new XrayInfo("SWFTE", summary, description, revision);
        xrayInfoWithUmlauts = new XrayInfo("SWFTE", "Dummy Umlaut Execution äöüßÄÖÜ", "dummy description mit Umlauten äöüßÄÖÜ", revision);
    }

    @Test
    public void testSearchForExistingTestExecution() {
        Optional<XrayTestExecutionIssue> optionalExistingTestExecution = defaultSummaryMapper.createXrayTestExecutionQuery(xrayInfo)
                .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());

        Assert.assertTrue(optionalExistingTestExecution.isPresent());
        Assert.assertEquals(optionalExistingTestExecution.get().getKey(), "SWFTE-799");
    }

    @Test
    public void testSearchForExistingTestExecutionContainingUmlauts() {
        Optional<XrayTestExecutionIssue> optionalExistingTestExecution = defaultSummaryMapper.createXrayTestExecutionQuery(xrayInfoWithUmlauts)
                .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());

        Assert.assertTrue(optionalExistingTestExecution.isPresent());
        Assert.assertEquals(optionalExistingTestExecution.get().getKey(), "SWFTE-798");
    }

    @Test
    public void testSearchForNonExistingTestExecution() {
        final XrayInfo nonExisitngExecution = xrayInfo;
        nonExisitngExecution.setRevision("not existing");

                Optional<XrayTestExecutionIssue> optionalExistingTestExecution = defaultSummaryMapper.createXrayTestExecutionQuery(nonExisitngExecution)
                .flatMap(jqlQuery -> xrayUtils.searchIssues(jqlQuery, XrayTestExecutionIssue::new).findFirst());
        Assert.assertFalse(optionalExistingTestExecution.isPresent());
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
