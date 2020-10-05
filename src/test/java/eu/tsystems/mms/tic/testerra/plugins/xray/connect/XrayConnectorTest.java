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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.SummaryContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.TestType;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.TestTypeEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.NotSyncableException;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.JiraUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang3.SerializationUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class XrayConnectorTest extends AbstractTest {

    private final String summary = "Dummy Execution";
    private final String description = "dummy description";
    private WebResource webResource;
    private XrayConfig xrayConfig;
    private String revision = "2017-04-10_dummy";
    private final XrayInfo xrayInfo = new XrayInfo("SWFTE", summary, description, revision);
    private final XrayInfo xrayInfoWithUmlauts = new XrayInfo("SWFTE", "Dummy Umlaut Execution äöüßÄÖÜ",
            "dummy description mit Umlauten äöüßÄÖÜ", revision);

    @BeforeTest
    public void prepareWebResource() throws URISyntaxException {
        webResource = eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils
                .prepareWebResource(Paths.get(getClass().getResource("/xray-test-posthoc.properties").toURI()).getFileName().toString());
        xrayConfig = XrayConfig.getInstance();
    }

    @Test
    public void testSearchForExistingTestExecution() throws Exception {
        final XrayConnector xrayConnector = new XrayConnector(xrayInfo);
        final Set<String> testExecKeys = xrayConnector.searchForExistingTestExecutions();
        assertEquals(testExecKeys.size(), 1);
        assertEquals(testExecKeys.iterator().next(), "SWFTE-799");
    }

    @Test
    public void testSearchForExistingTestExecutionContainingUmlauts() throws Exception {
        final XrayConnector xrayConnector = new XrayConnector(xrayInfoWithUmlauts);
        final Set<String> testExecKeys = xrayConnector.searchForExistingTestExecutions();
        assertEquals(testExecKeys.size(), 1);
        assertEquals(testExecKeys.iterator().next(), "SWFTE-798");
    }

    @Test
    public void testSearchForNonExistingTestExecution() throws Exception {
        final XrayInfo nonExisitngExecution = (XrayInfo) SerializationUtils.clone(xrayInfo);
        nonExisitngExecution.setRevision("not existing");
        final XrayConnector xrayConnector = new XrayConnector(nonExisitngExecution);
        final Set<String> testExecKeys = xrayConnector.searchForExistingTestExecutions();
        assertTrue(testExecKeys.isEmpty());
    }

    @Test
    public void testFindTestKeys() throws Exception {
        final XrayConnector xrayConnector = new XrayConnector(xrayInfo);
        final JqlQuery jqlQuery = JqlQuery.create()
                .addCondition(new SummaryContainsExact("passes"))
                .build();
        final Collection<String> testKeys = xrayConnector.findTestKeys("SWFTE-7", jqlQuery);
        assertEquals(testKeys.size(), 1);
        assertEquals(testKeys.iterator().next(), "SWFTE-1");
    }

    @Test
    public void testFindTestKeysNotExisting() throws Exception {
        final XrayConnector xrayConnector = new XrayConnector(xrayInfo);
        final JqlQuery jqlQuery = JqlQuery.create()
                .addCondition(new SummaryContainsExact("passes"))
                .addCondition(new TestTypeEquals(TestType.AutomatedCucumber))
                .build();
        final Collection<String> testKeys = xrayConnector.findTestKeys("SWFTE-7", jqlQuery);
        assertEquals(testKeys.size(), 0);
    }

    @Test
    public void testFindTestKeysMoreThanOneMatches() throws Exception {
        final XrayConnector xrayConnector = new XrayConnector(xrayInfo);
        final JqlQuery jqlQuery = JqlQuery.create()
                .addCondition(new SummaryContainsExact("TM"))
                .build();
        final Collection<String> testKeys = xrayConnector.findTestKeys("SWFTE-7", jqlQuery);
        assertEquals(testKeys.size(), 3);
    }

    @Test
    public void testFindOrCreateTestExecutionExisting() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        final Date oneMinuteAgo = calendar.getTime();

        final XrayConnector xrayConnector = new XrayConnector(xrayInfo);
        final String testExecutionKey = xrayConnector.findOrCreateTestExecution(Arrays.asList("SWFTE-1", "SWFTE-2", "SWFTE-3"));

        final JiraIssue issue = JiraUtils.getIssue(webResource, testExecutionKey, Arrays.asList(
                xrayConfig.getTestExecutionStartTimeFieldName(),
                xrayConfig.getTestExecutionFinishTimeFieldName()));
        final Date startTime =
                eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils.getDateFromField(issue, xrayConfig.getTestExecutionStartTimeFieldName());
        final Date finishTime =
                eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils.getDateFromField(issue, xrayConfig.getTestExecutionFinishTimeFieldName());

        final Date now = Calendar.getInstance().getTime();
        assertTrue(startTime.after(oneMinuteAgo), String.format("start time: '%s' is after one minute ago: '%s'", startTime, oneMinuteAgo));
        assertTrue(startTime.before(now), String.format("start time: '%s' is before now: '%s'", startTime, now));
        assertTrue(finishTime.after(oneMinuteAgo), String.format("finish time: '%s' is after one minute ago: '%s'", finishTime, oneMinuteAgo));
        assertTrue(finishTime.before(now), String.format("finish time: '%s' is before now: '%s'", finishTime, now));
    }

    @Test(dependsOnMethods = "testFindOrCreateTestExecutionExisting")
    public void testUpdateFinishTimeOfTestExecution() throws NotSyncableException, IOException, ParseException {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        final Date oneMinuteAgo = calendar.getTime();

        final String issueKey = "SWFTE-9";
        final XrayConnector xrayConnector = new XrayConnector(xrayInfo);
        xrayConnector.updateFinishTimeOfTestExecution(issueKey);
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        final JiraIssue issue = JiraUtils.getIssue(webResource, issueKey, Arrays.asList(xrayConfig.getTestExecutionFinishTimeFieldName()));

        final Date finishTime =
                eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils.getDateFromField(issue, xrayConfig.getTestExecutionFinishTimeFieldName());

        assertTrue(finishTime.before(Calendar.getInstance().getTime()));
        assertTrue(finishTime.after(oneMinuteAgo), String.format("finish time: %s, one miute ago: %s", finishTime, oneMinuteAgo));
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
