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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.GlobalTestData;
import eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.KeyInTestSetTests;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.ProjectEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.TestType;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.TestTypeEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatus;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraTransition;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraString;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraVerb;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.SimpleJiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.predef.SetLabels;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.utils.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class JiraUtilsTest extends AbstractTest implements Loggable {

    private WebResource webResource;
    private final String updateIssueKey = "SWFTE-802";
    private final String statusIssueKey = "SWFTE-809";
    private JiraUtils jiraUtils;
    private final String projectKey = PropertyManager.getProperty("xray.project.key");

    @BeforeTest
    public void prepareWebResource() throws URISyntaxException {
        webResource =
                TestUtils.prepareWebResource(Paths.get(getClass().getResource("/xray-test-posthoc.properties").toURI()).getFileName().toString());

        jiraUtils = new JiraUtils(webResource);
    }

    @DataProvider
    private Object[][] provideExistingIssues() {
        return new Object[][]{
                {"SWFTE-1", "TM - passes"},
                {"SWFTE-2", "TM - fails"},
                {"SWFTE-3", "TM - skips"},
                {"SWFTE-4", "TA - passes"},
                {"SWFTE-5", "TA - fails"},
                {"SWFTE-6", "TA - skips"}
        };
    }

    @Test(dataProvider = "provideExistingIssues")
    public void testGetIssue(final String key, final String summary) throws IOException {
        final JiraIssue issue = JiraUtils.getIssue(webResource, key);
        assertEquals(issue.getKey(), key);
        assertEquals(issue.getSummary(), summary);
        assertEquals(issue.getProject().getKey(), projectKey);
    }

    @Test
    public void test_updateIssueNew() throws IOException {
        JiraIssue issueToUpdate = new JiraIssue(updateIssueKey);
        issueToUpdate.setSummary("Neues Ticket: " + RandomUtils.generateRandomString());
        List<String> labels = issueToUpdate.getLabels();
        labels.add("Testerra");
        labels.add("XRay");
        labels.add("Connector");
        jiraUtils.createOrUpdateIssue(issueToUpdate);

        JiraIssue updatedIssue = jiraUtils.getIssue(issueToUpdate.getKey());
        assertEquals(updatedIssue.getSummary(), issueToUpdate.getSummary());
        assertEquals(updatedIssue.getLabels().stream().sorted().collect(Collectors.toList()), labels.stream().sorted().collect(Collectors.toList()));
    }

    @Test
    public void test_updateIssueWithoutChange() throws IOException {
        final String expectedSummary = "Testerra Xray Connector Test Ticket";
        JiraIssue issueToUpdate = new JiraIssue(updateIssueKey);
        issueToUpdate.setSummary(expectedSummary);
        jiraUtils.createOrUpdateIssue(issueToUpdate);

        JiraIssue updatedIssue = jiraUtils.getIssue(issueToUpdate.getKey());
        assertEquals(updatedIssue.getSummary(), issueToUpdate.getSummary());

        jiraUtils.createOrUpdateIssue(issueToUpdate);
        assertEquals(updatedIssue.getSummary(), issueToUpdate.getSummary());
    }

//    @Test
//    public void test_createIssue() throws IOException {
//        final String expectedSummary = "Testerra Xray Connector New Test Ticket";
//        JiraIssue newIssue = new JiraIssue();
//        newIssue.setSummary(expectedSummary);
//        newIssue.getProject().setKey(projectKey);
//        newIssue.setIssueType(IssueType.Test.getIssueType());
//        newIssue.setDescription("Dies ist ein Test zum Anlegen von Tests");
//        jiraUtils.createOrUpdateIssue(newIssue);
//        log().info("Create issue " + newIssue.getKey());
//        assertNotNull(newIssue.getKey());
//        assertNotNull(newIssue.getProject().getKey());
//
//        JiraIssue createdIssue = jiraUtils.getIssue(newIssue.getKey());
//        assertEquals(createdIssue.getSummary(), newIssue.getSummary());
//        assertEquals(createdIssue.getDescription(), newIssue.getDescription());
//        assertEquals(createdIssue.getProject().getKey(), newIssue.getProject().getKey());
//    }

    @Test
    public void testUpdateIssue() throws IOException {
        final SimpleJiraIssueUpdate jiraIssueUpdate = new SimpleJiraIssueUpdate();
        final String randomizedDescription = RandomUtils.generateRandomString();

        final ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("description", randomizedDescription);

        jiraIssueUpdate.setFields(objectNode);
        JiraUtils.updateIssue(webResource, updateIssueKey, jiraIssueUpdate);
        final JiraIssue issue = JiraUtils.getIssue(webResource, updateIssueKey, Lists.newArrayList("description"));
        assertEquals(issue.getKey(), updateIssueKey);
        assertEquals(issue.getDescription(), randomizedDescription);
    }

    @Test
    public void testUpdateIssueWithPredefsRemoveAllLabels() throws IOException {
        String[] labelsToSet = new String[]{};
        final JiraIssueUpdate update = JiraIssueUpdate.create()
                .field(new SetLabels(labelsToSet))
                .build();

        JiraUtils.updateIssue(webResource, updateIssueKey, update);
        final JiraIssue issue = JiraUtils.getIssue(webResource, updateIssueKey, Lists.newArrayList("labels"));
        assertEquals(issue.getKey(), updateIssueKey);
        Assert.assertTrue(issue.getLabels().isEmpty());
    }

    @Test(dependsOnMethods = "testUpdateIssueWithPredefsRemoveAllLabels")
    public void testUpdateIssueWithPredefsSetLabels() throws IOException {
        String[] labelsToSet = new String[]{"Test-Automatisierung", "Aufräumaktion"};
        final JiraIssueUpdate update = JiraIssueUpdate.create()
                .field(new SetLabels(labelsToSet))
                .build();

        JiraUtils.updateIssue(webResource, updateIssueKey, update);
        final JiraIssue issue = JiraUtils.getIssue(webResource, updateIssueKey, Lists.newArrayList("labels"));
        assertEquals(issue.getKey(), updateIssueKey);
        final List<String> foundLabels = issue.getLabels();
        Assert.assertTrue(foundLabels.containsAll(Arrays.asList(labelsToSet)));
        Assert.assertTrue(Arrays.asList(labelsToSet).containsAll(foundLabels));
    }

    @Test(dependsOnMethods = "testUpdateIssueWithPredefsSetLabels")
    public void testUpdateIssueWithPredefsRemoveSingleLabel() throws IOException {
        final JiraIssueUpdate update = JiraIssueUpdate.create()
                .field("labels", JiraVerb.REMOVE, new JiraString("Aufräumaktion"))
                .build();

        JiraUtils.updateIssue(webResource, updateIssueKey, update);
        final JiraIssue issue = JiraUtils.getIssue(webResource, updateIssueKey, Lists.newArrayList("labels"));
        assertEquals(issue.getKey(), updateIssueKey);
        final List<String> foundLabels = issue.getLabels();
        Assert.assertEquals(foundLabels.size(), 1);
        Assert.assertTrue(foundLabels.contains("Test-Automatisierung"));
        Assert.assertTrue(!foundLabels.contains("Aufräumaktion"));
    }

    @Test
    public void testCreateTestExecution() throws IOException, ParseException {
        final String projectKey = "SWFTE";
        final String summary = RandomUtils.generateRandomString();
        final String description = RandomUtils.generateRandomString();
        final String revision = RandomUtils.generateRandomString();
        final List<String> testEnvironments = ImmutableList.of("Android", "Samsung");

        final XrayInfo xrayInfo = new XrayInfo(projectKey, summary, description, testEnvironments, revision, "1.0.2", "fnu-jira-testerra");
        final String key = JiraUtils.createTestExecutionGeneric(webResource, xrayInfo);

        final JiraIssue rawIssue = JiraUtils.getIssue(webResource, key,
                Lists.newArrayList("project", "summary", "description",
                        Fields.REVISION.getFieldName(),
                        Fields.TEST_EXECUTION_START_DATE.getFieldName(),
                        Fields.TEST_ENVIRONMENTS.getFieldName()
                )
        );
        XrayTestExecutionIssue issue = new XrayTestExecutionIssue(rawIssue);
        assertEquals(issue.getKey(), key);
        assertEquals(issue.getProject().getName(), "Spielwiese Framework-Tests");
        assertEquals(issue.getSummary(), summary);
        assertEquals(issue.getDescription(), description);
        assertEquals(issue.getRevision(), revision);
        issue.getTestEnvironments()
                .forEach(x -> assertTrue(testEnvironments.contains(x)));

        final Date startDate = issue.getStartDate();
        assertNotNull(issue.getStartDate());
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        assertTrue(startDate.before(Calendar.getInstance().getTime()));
        assertTrue(startDate.after(calendar.getTime()), String.format("finish time: %s, calendar time: %s", startDate, calendar.getTime()));

        /* set global property to use later */
        GlobalTestData.getInstance().setKeyOfNewTestExecution(key);
    }

    @Test(groups = "issueStatus")
    public void testGetIssueStatus() throws IOException {
        final JiraStatus issueStatus = JiraUtils.getIssueStatus(webResource, statusIssueKey);
        assertEquals(issueStatus.getName(), "Erneut geöffnet");
    }

    @Test(dependsOnMethods = "testGetIssueStatus")
    public void testGetTransistions() throws IOException {
        final Set<JiraTransition> transitions = JiraUtils.getTransitions(webResource, statusIssueKey);
        final HashSet<String> expectedTransitionNames = Sets.newHashSet("Arbeit beginnen", "Information anfordern");
        final HashSet<String> foundTransitionNames = new HashSet<>();

        for (final JiraTransition transition : transitions) {
            foundTransitionNames.add(transition.getName());
        }
        assertTrue(expectedTransitionNames.size() == foundTransitionNames.size()
                && expectedTransitionNames.containsAll(foundTransitionNames)
                && foundTransitionNames.containsAll(expectedTransitionNames));
    }

    @Test(dependsOnMethods = "testGetTransistions")
    public void testChangeIssueStatus() throws IOException {
        final JiraStatus originalStatus = JiraUtils.getIssueStatus(webResource, statusIssueKey);
        Assert.assertEquals(originalStatus.getName(), "Erneut geöffnet");

        JiraUtils.doTransitionByName(webResource, statusIssueKey, "Arbeit beginnen");
        JiraStatus newStatus = JiraUtils.getIssueStatus(webResource, statusIssueKey);
        assertEquals(newStatus.getName(), "In Arbeit");

        JiraUtils.doTransitionByName(webResource, statusIssueKey, "Arbeit beenden");
        newStatus = JiraUtils.getIssueStatus(webResource, statusIssueKey);
        assertEquals(newStatus.getName(), "Erledigt");

        JiraUtils.doTransitionByName(webResource, statusIssueKey, "Wieder öffnen");
        newStatus = JiraUtils.getIssueStatus(webResource, statusIssueKey);
        assertEquals(newStatus.getName(), originalStatus.getName());
    }

    @Test
    public void testUploadJsonAttachment() throws IOException {
        final String issueKey = "SWFTE-12";
        final String attachmentFilename = "testBliBlaBlubb.json";
        JiraUtils.uploadJsonAttachment(webResource, issueKey, "content", attachmentFilename);
        final JiraIssue issue = JiraUtils.getIssue(webResource, issueKey, Lists.newArrayList("attachment"));
        Assert.assertEquals(issue.getAttachments().size(), 1);
        final String foundFileName = issue.getAttachments().get(0).getFilename();
        //TODO also assert content of file
        assertEquals(foundFileName, attachmentFilename);
    }

    @Test(dependsOnMethods = {"testUploadJsonAttachment"})
    public void testUploadAttachment() throws IOException {
        final String issueKey = "SWFTE-12";
        final InputStream is = getClass().getResourceAsStream("/archive.zip");
        final String attachmentFilename = "archive.zip";
        JiraUtils.uploadAttachment(webResource, issueKey, is, attachmentFilename);
        final JiraIssue issue = JiraUtils.getIssue(webResource, issueKey, Lists.newArrayList("attachment"));
        final String foundFileName = issue.getAttachments().get(0).getFilename();
        //TODO also assert content of file
        assertEquals(foundFileName, attachmentFilename);
    }

    @Test(dependsOnMethods = {"testUploadAttachment"})
    public void testDeleteAllAttachments() throws IOException {
        final String issueKey = "SWFTE-12";

        final JiraIssue originalIssue = JiraUtils.getIssue(webResource, issueKey, Lists.newArrayList("attachment"));
        int attachmentCount = originalIssue.getAttachments().size();
        assertTrue(attachmentCount > 0);

        JiraUtils.deleteAllAttachments(webResource, issueKey);

        final JiraIssue nakedIssue = JiraUtils.getIssue(webResource, issueKey, Lists.newArrayList("attachment"));
        attachmentCount = nakedIssue.getAttachments().size();
        assertEquals(attachmentCount, 0);
    }

    @Test
    public void testSearchIssues() throws Exception {
        final JqlQuery jqlQuery = JqlQuery.create()
                .addCondition(new ProjectEquals("SWFTE"))
                .addCondition(new TestTypeEquals(TestType.AutomatedGeneric))
                .addCondition(new KeyInTestSetTests("SWFTE-8"))
                .build();
        final Set<JiraIssue> jiraIssues = JiraUtils.searchIssues(webResource, jqlQuery.createJql());
        //        final Collection<String> foundKeys = Collections2.transform(jiraIssues, JiraIssue::getKey);
        final List<String> foundKeys = jiraIssues.stream().map(JiraIssue::getKey).collect(Collectors.toList());
        assertTrue(foundKeys.containsAll(Arrays.asList("SWFTE-4", "SWFTE-5", "SWFTE-6")));
    }
}
