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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.KeyInTestSetTests;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.ProjectEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.TestType;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.TestTypeEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatus;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatusCategory;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraTransition;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraString;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraVerb;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import java.util.ArrayList;
import java.util.Optional;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class JiraUtilsTest extends AbstractTest implements Loggable {

    private final String updateIssueKey = "SWFTE-802";
    private final String statusIssueKey = "SWFTE-809";
    private JiraUtils jiraUtils;

    @BeforeTest
    public void prepareWebResource() throws URISyntaxException {
        super.prepareWebResource();
        jiraUtils = new JiraUtils(webResource);
    }

    @DataProvider
    private Object[][] provideExistingTestIssues() {
        return new Object[][]{
                {"SWFTE-1", "TM - passes"},
                {"SWFTE-2", "TM - fails"},
                {"SWFTE-3", "TM - skips"},
                {"SWFTE-4", "TA - passes"},
                {"SWFTE-5", "TA - fails"},
                {"SWFTE-6", "TA - skips"}
        };
    }

    @Test(dataProvider = "provideExistingTestIssues")
    public void testGetIssue(final String key, final String summary) throws IOException {
        final JiraIssue issue = JiraUtils.getIssue(webResource, key);
        assertEquals(issue.getKey(), key);
        assertEquals(issue.getSummary(), summary);
        assertEquals(issue.getProject().getKey(), projectKey);
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

    @Test
    public void test_createTestSet() throws IOException {
        final String testToLink = "SWFTE-1";
        XrayTestSetIssue testSet = new XrayTestSetIssue();
        testSet.setSummary("Testerra Xray Connector TestSet");
        testSet.getProject().setKey(projectKey);
        testSet.setDescription("Test set");
        testSet.setTestKeys(Lists.newArrayList(testToLink));

        jiraUtils.createOrUpdateIssue(testSet);

        assertNotNull(testSet.getKey());
        log().info("Created test set: " + testSet.getKey());

        XrayTestSetIssue updatedIssue = jiraUtils.getIssue(testSet.getKey(), XrayTestSetIssue::new);
        assertEquals(updatedIssue.getKey(), testSet.getKey());
        assertEquals(testSet.getTestKeys().get(0), testToLink);
    }

    @Test
    public void testUpdateIssueWithPredefsRemoveAllLabels() throws IOException {
        JiraIssue issueToUpdate = new JiraIssue(updateIssueKey);
        issueToUpdate.setLabels(new ArrayList<>());
        jiraUtils.createOrUpdateIssue(issueToUpdate);

        JiraIssue updatedIssue = jiraUtils.getIssue(updateIssueKey);
        Assert.assertTrue(updatedIssue.getLabels().isEmpty());
    }

    @Test(dependsOnMethods = "testUpdateIssueWithPredefsRemoveAllLabels")
    public void testUpdateIssueWithPredefsSetLabels() throws IOException {
        JiraIssue issueToUpdate = new JiraIssue(updateIssueKey);
        List<String> labels = issueToUpdate.getLabels();
        labels.add("Test-Automatisierung");
        labels.add("Aufräumaktion");
        jiraUtils.createOrUpdateIssue(issueToUpdate);

        JiraIssue updatedIssue = jiraUtils.getIssue(updateIssueKey);
        assertEquals(updatedIssue.getLabels().stream().sorted().collect(Collectors.toList()), labels.stream().sorted().collect(Collectors.toList()));
    }

    @Test()
    public void testUpdateIssueWithPredefsRemoveSingleLabel() throws IOException {
        JiraIssue existingIssue = jiraUtils.getIssue(updateIssueKey);
        existingIssue.getLabels().removeIf(s -> s.equals("Aufräumaktion"));
        JiraIssue issueToUpdate = new JiraIssue(updateIssueKey);
        issueToUpdate.setLabels(existingIssue.getLabels());
        jiraUtils.createOrUpdateIssue(issueToUpdate);

        JiraIssue updatedIssue = jiraUtils.getIssue(updateIssueKey);
        final List<String> foundLabels = updatedIssue.getLabels();
        Assert.assertEquals(foundLabels.size(), 1);
        Assert.assertTrue(foundLabels.contains("Test-Automatisierung"));
        Assert.assertTrue(!foundLabels.contains("Aufräumaktion"));
    }

    @Test(enabled = false)
    public void test_performTransition_newApi() throws IOException {
        Set<JiraTransition> transitions = jiraUtils.getAvailableTransitions(statusIssueKey);
        Optional<JiraTransition> transitionByStatusCategory = jiraUtils.getTransitionByStatusCategory(transitions, JiraStatusCategory.DONE);
        Assert.assertTrue(transitionByStatusCategory.isPresent(), String.format("Transition to %s is not available", JiraStatusCategory.DONE.getKey()));
        jiraUtils.performTransition(statusIssueKey, transitionByStatusCategory.get());
    }

    @Test(groups = "issueStatus")
    public void testGetIssueStatus() throws IOException {
        final JiraStatus issueStatus = JiraUtils.getIssueStatus(webResource, statusIssueKey);
        assertEquals(issueStatus.getName(), "Erneut geöffnet");
    }

    @Test(dependsOnMethods = "testGetIssueStatus")
    public void testGetTransitions() throws IOException {
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
        JiraStatus newStatus;
        JiraStatus originalStatus = JiraUtils.getIssueStatus(webResource, statusIssueKey);
        Assert.assertEquals(originalStatus.getName(), "Erneut geöffnet");

        JiraUtils.doTransitionByName(webResource, statusIssueKey, "Arbeit beginnen");
        newStatus = JiraUtils.getIssueStatus(webResource, statusIssueKey);
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
