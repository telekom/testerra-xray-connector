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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.utils.RandomUtils;
import java.io.IOException;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class XrayUtilsTest extends AbstractTest implements Loggable {

    private XrayUtils xrayUtils;
    private static final String EXISTING_TEST_EXECUTION_KEY = "SWFTE-9";
    private static final String EXISTING_TEST_PLAN_KEY = "SWFTE-1083";
    private static final Set<String> EXISTING_TEST_KEYS = Sets.newHashSet("SWFTE-1", "SWFTE-2", "SWFTE-3");
    private static String CREATED_TEST_EXECUTION_KEY;

    @BeforeTest
    public void prepareWebResource() throws URISyntaxException {
        super.prepareWebResource();
        xrayUtils = new XrayUtils(webResource);
    }

    /**
     * This test requires field ids to be set.
     * See "Custom Jira field IDs" section in README.md
     */
    @Test
    public void test_createTestSet() throws IOException {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        final String projectKey = xrayConfig.getProjectKey();
        Assert.assertFalse(StringUtils.isBlank(projectKey));

        final String testToLink = "SWFTE-1";
        XrayTestSetIssue testSet = new XrayTestSetIssue();
        testSet.setSummary("Testerra Xray Connector TestSet");
        testSet.getProject().setKey(projectKey);
        testSet.setDescription("Test set");
        testSet.setTestKeys(Lists.newArrayList(testToLink));

        xrayUtils.createOrUpdateIssue(testSet);

        assertNotNull(testSet.getKey());
        log().info("Created test set: " + xrayConfig.getIssueUrl(testSet.getKey()).orElse(null));

        XrayTestSetIssue updatedIssue = xrayUtils.getIssue(testSet.getKey(), XrayTestSetIssue::new);
        assertEquals(updatedIssue.getKey(), testSet.getKey());
        assertEquals(testSet.getTestKeys().get(0), testToLink);
    }

    /**
     * This test requires field ids to be set.
     * See "Custom Jira field IDs" section in README.md
     */
    @Test
    public void test_importTestExecution() throws IOException {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        final String projectKey = xrayConfig.getProjectKey();
        Assert.assertFalse(StringUtils.isBlank(projectKey));
        final List<String> testEnvironments = ImmutableList.of("NewApi", "XrayTestExecutionIssue");
        final String summary = RandomUtils.generateRandomString() + "äöüßÄÖÜ";
        final String description = RandomUtils.generateRandomString() + "äöüßÄÖÜ";
        final String revision = RandomUtils.generateRandomString() + "äöüßÄÖÜ";
        final List<String> testPlanKeys = ImmutableList.of(EXISTING_TEST_PLAN_KEY);
        final List<String> versionNames = ImmutableList.of("1.0", "1.1");

        final XrayTestExecutionIssue issueToImport = new XrayTestExecutionIssue();
        issueToImport.getProject().setKey(projectKey);
        issueToImport.setDescription(description);
        issueToImport.setSummary(summary);
        issueToImport.setRevision(revision);
        issueToImport.setTestEnvironments(testEnvironments);
        issueToImport.setTestPlanKeys(testPlanKeys);
        issueToImport.setFixVersions(versionNames.stream().map(JiraNameReference::new).collect(Collectors.toList()));

        final XrayTestExecutionImport xrayTestExecutionImport = new XrayTestExecutionImport(issueToImport);
        xrayTestExecutionImport.setTestKeys(EXISTING_TEST_KEYS, XrayTestExecutionImport.Test.Status.TODO);

        // Validate the import model
        final XrayTestExecutionImport.Info info = xrayTestExecutionImport.getInfo();
        Assert.assertEquals(info.getProject(), projectKey);
        Assert.assertEquals(info.getSummary(), summary);
        Assert.assertEquals(info.getRevision(), revision);
        Assert.assertTrue(info.getTestEnvironments().containsAll(testEnvironments));
        Assert.assertEquals(info.getTestPlanKey(), EXISTING_TEST_PLAN_KEY);
        Assert.assertEquals(info.getVersion(), versionNames.get(0));

        xrayUtils.importTestExecution(xrayTestExecutionImport);
        CREATED_TEST_EXECUTION_KEY = xrayTestExecutionImport.getTestExecutionKey();
        log().info("Created TestExecution: " + xrayConfig.getIssueUrl(CREATED_TEST_EXECUTION_KEY).orElse(null));

        final XrayTestExecutionIssue importedIssue = xrayUtils.getIssue(xrayTestExecutionImport.getTestExecutionKey(), XrayTestExecutionIssue::new);
        Assert.assertEquals(importedIssue.getDescription(), description);
        Assert.assertEquals(importedIssue.getSummary(), summary);
        Assert.assertEquals(importedIssue.getRevision(), revision);
        Assert.assertTrue(importedIssue.getTestEnvironments().containsAll(testEnvironments));
        Assert.assertEquals(importedIssue.getTestPlanKeys().get(0), EXISTING_TEST_PLAN_KEY);
        Assert.assertEquals(importedIssue.getFixVersions().get(0).getName(), versionNames.get(0));

        final Calendar calAgo = Calendar.getInstance();
        calAgo.add(Calendar.MINUTE, -2);
        final Date startDate = importedIssue.getStartDate();
        assertNotNull(startDate);
        final Calendar calInFuture = Calendar.getInstance();
        calInFuture.add(Calendar.MINUTE, 2);
        assertTrue(startDate.before(calInFuture.getTime()), String.format("start time %s is before %s", startDate, calInFuture.getTime()));
        assertTrue(startDate.after(calAgo.getTime()), String.format("start time %s is after %s", startDate, calAgo.getTime()));

        XrayTestExecutionImport.Test[] tests = xrayUtils.getTestsByTestExecutionKey(importedIssue.getKey());
        EXISTING_TEST_KEYS.forEach(testKey -> {
            Assert.assertTrue(Arrays.stream(tests).anyMatch(test -> test.getTestKey().equals(testKey)));
        });
    }

    @Test(dependsOnMethods = "test_importTestExecution")
    public void test_extendTestExecutionByExistingTest() throws IOException {
        Assert.assertNotNull(CREATED_TEST_EXECUTION_KEY);
        final String issueToAdd = "SWFTE-4";
        XrayTestExecutionImport xrayTestExecutionImport = new XrayTestExecutionImport(CREATED_TEST_EXECUTION_KEY);
        xrayTestExecutionImport.addTestKeys(Sets.newHashSet(issueToAdd), XrayTestExecutionImport.Test.Status.TODO);
        xrayUtils.importTestExecution(xrayTestExecutionImport);
        XrayTestExecutionImport.Test[] tests = xrayUtils.getTestsByTestExecutionKey(xrayTestExecutionImport.getTestExecutionKey());
        Assert.assertTrue(Arrays.stream(tests).anyMatch(test -> test.getTestKey().equals(issueToAdd)));
        EXISTING_TEST_KEYS.forEach(testKey -> {
            Assert.assertTrue(Arrays.stream(tests).anyMatch(test -> test.getTestKey().equals(testKey)));
        });
    }

    @Test(dependsOnMethods = "test_importTestExecution")
    public void testExportTestExecutionAsJson() throws Exception {
        Assert.assertNotNull(CREATED_TEST_EXECUTION_KEY);
        final String json = XrayUtils.exportTestExecutionAsJson(webResource, CREATED_TEST_EXECUTION_KEY);
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.readValue(json, XrayTestExecutionImport.Test[].class);
    }

    @Test(enabled = false)
    public void testSyncTestExecutionWithEvidences() throws Exception {
        final String issueKey = "BLA-13209";
        final XrayTestExecutionImport execution = new XrayTestExecutionImport(issueKey);
        final Set<XrayTestExecutionImport.Test> existingTests = XrayUtils.getTestsFromExecution(webResource, issueKey);
        final XrayTestExecutionImport.Test.Status status = Arrays.asList(XrayTestExecutionImport.Test.Status.values()).get(new Random().nextInt(XrayTestExecutionImport.Test.Status.values().length));

        XrayTestExecutionImport.Test testBLA13138 = null;
        for (final XrayTestExecutionImport.Test testIssue : existingTests) {
            testIssue.setStatus(status);
            final Date now = new Date();
            testIssue.setStart(now);
            testIssue.setFinish(now);
            final String testKey = testIssue.getTestKey();
            if (testKey.equals("BLA-13138")) {
                testBLA13138 = testIssue;
            }
        }

        final XrayTestExecutionImport.Test.Evidence evidence = new XrayTestExecutionImport.Test.Evidence();
        evidence.setData("YmxhIGJsdWJiDQo=");
        evidence.setFilename("test.txt");
        evidence.setContentType(MediaType.TEXT_PLAIN_TYPE);
        final String html = "<html><head><title>First parse</title></head>"
                + "<body><p>Bli Bla Blubb.</p></body></html>";
        final XrayTestExecutionImport.Test.Evidence htmlEvidence = new XrayTestExecutionImport.Test.Evidence();
        htmlEvidence.setData("PGh0bWw+PGhlYWQ+PHRpdGxlPkZpcnN0IHBhcnNlPC90aXRsZT48L2hlYWQ+PGJvZHk+PHA+QmxpIEJsYSBCbHViYi48L3A+PC9ib2R5PjwvaHRtbD4=");
        htmlEvidence.setFilename("test.html");
        htmlEvidence.setContentType(MediaType.TEXT_PLAIN_TYPE);

        final XrayTestExecutionImport.Test.Evidence zipEvidence = new XrayTestExecutionImport.Test.Evidence();

        final byte[] bytes = Files.readAllBytes(Paths.get(getClass().getResource("/archive.zip").toURI()));
        final String base64String = Base64.encodeBase64String(bytes);

        zipEvidence.setData(base64String);
        zipEvidence.setFilename("test.zip");
        zipEvidence.setContentType(MediaType.WILDCARD_TYPE);

        if (testBLA13138 != null) {
            //            final Set<XrayEvidence> evidences = testBLA13138.getEvidences();
            //            evidences.addAll(Arrays.asList(evidence, htmlEvidence, zipEvidence));
            //            testBLA13138.setEvidences(evidences);
            final HashSet<XrayTestExecutionImport.Test.Evidence> xrayEvidences = new HashSet<>();
            xrayEvidences.addAll(Arrays.asList(htmlEvidence, zipEvidence));
            testBLA13138.setEvidences(xrayEvidences);
        }

        execution.setTests(existingTests);
        XrayUtils.syncTestExecutionReturnKey(webResource, execution);

        boolean textFileFound = false;
        boolean zipFileFound = false;
        final Set<XrayTestExecutionImport.Test> testIssues = XrayUtils.getTestsFromExecution(webResource, issueKey);
        for (final XrayTestExecutionImport.Test testIssue : testIssues) {
            for (final XrayTestExecutionImport.Test.Evidence xrayEvidence : testIssue.getEvidences()) {
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
