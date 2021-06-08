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

package eu.tsystems.mms.tic.testerra.plugins.xray.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.GlobalTestData;
import eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.FreshXrayTestExecution;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.UpdateXrayTestExecution;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayEvidence;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestStatus;
import eu.tsystems.mms.tic.testframework.utils.RandomUtils;
import org.apache.commons.codec.binary.Base64;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class XrayUtilsTest extends AbstractTest {

    private WebResource webResource;
    private XrayConfig xrayConfig;

    @BeforeTest
    public void prepareWebResource() throws URISyntaxException {
        webResource =
                TestUtils.prepareWebResource(Paths.get(getClass().getResource("/xray-test-posthoc.properties").toURI()).getFileName().toString());
    }

    @Test
    public void testCreateTestExecution() throws Exception {
        final Calendar calAgo = Calendar.getInstance();
        calAgo.add(Calendar.MINUTE, -2);

        final String projectKey = "SWFTE";
        final String summary = RandomUtils.generateRandomString() + "äöüßÄÖÜ";
        final String description = RandomUtils.generateRandomString() + "äöüßÄÖÜ";
        final String revision = RandomUtils.generateRandomString() + "äöüßÄÖÜ";
        final List<String> testEnvironments = ImmutableList.of("Android", "Samsung");

        final XrayInfo xrayInfo = new XrayInfo(projectKey, summary, description, revision);
        xrayInfo.setTestEnvironments(testEnvironments);
        final FreshXrayTestExecution freshTestExecution =
                XrayUtils.createFreshTestExecution(xrayInfo, Arrays.asList("SWFTE-1", "SWFTE-2", "SWFTE-3"));
        final String key = XrayUtils.syncTestExecutionReturnKey(webResource, freshTestExecution);

        final JiraIssue issue = JiraUtils.getIssue(webResource, key,
                Lists.newArrayList("project", "summary", "description",
                        Fields.REVISION.getFieldName(),
                        Fields.TEST_EXECUTION_START_DATE.getFieldName(),
                        Fields.TEST_ENVIRONMENTS.getFieldName()
                )
        );
        Assert.assertEquals(issue.getKey(), key);
        Assert.assertEquals(issue.getFields().findValue("project").findValue("name").asText(), "Spielwiese Framework-Tests");
        Assert.assertEquals(issue.getFields().findValue("summary").asText(), summary);
        Assert.assertEquals(issue.getFields().findValue("description").asText(), description);
        Assert.assertEquals(issue.getFields().findValue(Fields.REVISION.getFieldName()).asText(), revision);
        issue.getFields().findValue(Fields.TEST_ENVIRONMENTS.getFieldName())
                .forEach(x -> assertTrue(testEnvironments.contains(x.textValue())));

        final JsonNode startDateNode = issue.getFields().findValue(Fields.TEST_EXECUTION_START_DATE.getFieldName());
        assertNotNull(startDateNode);
        final Date startDate = JiraUtils.dateFormat.parse(startDateNode.asText());
        final Calendar calInFuture = Calendar.getInstance();
        calInFuture.add(Calendar.MINUTE, 2);
        assertTrue(startDate.before(calInFuture.getTime()), String.format("start time %s is before %s", startDate, calInFuture.getTime()));
        assertTrue(startDate.after(calAgo.getTime()), String.format("start time %s is after %s", startDate, calAgo.getTime()));

        /** set global property to use later */
        GlobalTestData.getInstance().setKeyOfNewTestExecution(key);
    }

    @Test(dependsOnMethods = "testCreateTestExecution")
    public void testExportTestExecutionAsJson() throws Exception {
        final String issueKey = GlobalTestData.getInstance().getKeyOfNewTestExecution();
        final String json = XrayUtils.exportTestExecutionAsJson(webResource, issueKey);
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.readValue(json, XrayTestIssue[].class);
    }

    @Test
    public void testSyncTestExecution() throws Exception {
        final String testExecutionKey = "SWFTE-9";
        final Set<XrayTestIssue> existingTests = XrayUtils.getTestsFromExecution(webResource, testExecutionKey);

        // statuses is used for clarity here
        final XrayTestStatus randomStatuses = Arrays.asList(XrayTestStatus.values()).get(new Random().nextInt(XrayTestStatus.values().length));

        for (final XrayTestIssue testIssue : existingTests) {
            testIssue.setStatus(randomStatuses);
            final Date now = new Date();
            testIssue.setStart(now);
            testIssue.setFinish(now);
        }

        final UpdateXrayTestExecution execution = new UpdateXrayTestExecution(testExecutionKey);
        execution.setTests(existingTests);
        XrayUtils.syncTestExecutionReturnKey(webResource, execution);
        //TODO: check execution
    }

    @Test(enabled = false)
    public void testSyncTestExecutionWithEvidences() throws Exception {
        final String issueKey = "BLA-13209";
        final UpdateXrayTestExecution execution = new UpdateXrayTestExecution(issueKey);
        final Set<XrayTestIssue> existingTests = XrayUtils.getTestsFromExecution(webResource, issueKey);
        final XrayTestStatus status = Arrays.asList(XrayTestStatus.values()).get(new Random().nextInt(XrayTestStatus.values().length));

        XrayTestIssue testBLA13138 = null;
        for (final XrayTestIssue testIssue : existingTests) {
            testIssue.setStatus(status);
            final Date now = new Date();
            testIssue.setStart(now);
            testIssue.setFinish(now);
            final String testKey = testIssue.getTestKey();
            if (testKey.equals("BLA-13138")) {
                testBLA13138 = testIssue;
            }
        }

        final XrayEvidence evidence = new XrayEvidence();
        evidence.setData("YmxhIGJsdWJiDQo=");
        evidence.setFilename("test.txt");
        evidence.setContentType(MediaType.TEXT_PLAIN_TYPE);
        final String html = "<html><head><title>First parse</title></head>"
                + "<body><p>Bli Bla Blubb.</p></body></html>";
        final XrayEvidence htmlEvidence = new XrayEvidence();
        htmlEvidence.setData("PGh0bWw+PGhlYWQ+PHRpdGxlPkZpcnN0IHBhcnNlPC90aXRsZT48L2hlYWQ+PGJvZHk+PHA+QmxpIEJsYSBCbHViYi48L3A+PC9ib2R5PjwvaHRtbD4=");
        htmlEvidence.setFilename("test.html");
        htmlEvidence.setContentType(MediaType.TEXT_PLAIN_TYPE);

        final XrayEvidence zipEvidence = new XrayEvidence();

        final byte[] bytes = Files.readAllBytes(Paths.get(getClass().getResource("/archive.zip").toURI()));
        final String base64String = Base64.encodeBase64String(bytes);

        zipEvidence.setData(base64String);
        zipEvidence.setFilename("test.zip");
        zipEvidence.setContentType(MediaType.WILDCARD_TYPE);

        if (testBLA13138 != null) {
            //            final Set<XrayEvidence> evidences = testBLA13138.getEvidences();
            //            evidences.addAll(Arrays.asList(evidence, htmlEvidence, zipEvidence));
            //            testBLA13138.setEvidences(evidences);
            final HashSet<XrayEvidence> xrayEvidences = new HashSet<>();
            xrayEvidences.addAll(Arrays.asList(htmlEvidence, zipEvidence));
            testBLA13138.setEvidences(xrayEvidences);
        }

        execution.setTests(existingTests);
        XrayUtils.syncTestExecutionReturnKey(webResource, execution);

        boolean textFileFound = false;
        boolean zipFileFound = false;
        final Set<XrayTestIssue> testIssues = XrayUtils.getTestsFromExecution(webResource, issueKey);
        for (final XrayTestIssue testIssue : testIssues) {
            for (final XrayEvidence xrayEvidence : testIssue.getEvidences()) {
                if (xrayEvidence.getFilename().equals("test.txt")) {
                    // TODO: check content of txt file
                    textFileFound = true;
                }
                if (xrayEvidence.getFilename().equals("test.zip")) {
                    // TODO: check content of zip file
                    zipFileFound = true;
                }
            }

        }

        Assert.assertTrue(textFileFound, "text file not found");
        Assert.assertTrue(zipFileFound, "zip file not found");
    }

}
