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

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraNullValue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraVerb;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.predef.SetLabels;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.UpdateXrayTestExecution;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestStatus;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.JiraUtils;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import eu.tsystems.mms.tic.testframework.events.TesterraEventListener;
import eu.tsystems.mms.tic.testframework.events.TesterraEventService;
import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public abstract class SimulatedRunTest extends TesterraTest {

    protected Map<String, XrayTestStatus> fullWithoutParametrized = ImmutableMap.of(
            "SWFTE-4", XrayTestStatus.PASS,
            "SWFTE-5", XrayTestStatus.FAIL,
            "SWFTE-6", XrayTestStatus.SKIPPED
    );
    private WebResource webResource;
    private XrayConfig xrayConfig;
    private Map<String, XrayTestStatus> expectedTestResultsFullSet = ImmutableMap.of(
            "SWFTE-4", XrayTestStatus.PASS,
            "SWFTE-5", XrayTestStatus.FAIL,
            "SWFTE-6", XrayTestStatus.SKIPPED,
            "SWFTE-319", XrayTestStatus.PASS,
            "SWFTE-320", XrayTestStatus.FAIL
    );

    abstract String getExpectedSummary();

    abstract String getExpectedDescription();

    abstract String getExpectedRevision();

    private List<String> getExpectedAffectedVersions() {
        return newArrayList("1.0.2", "1.0.3");
    }

    private List<String> getExpectedLabels() {
        return newArrayList("Test-Automatisierung");
    }

    @Parameters("propertiesFileName")
    @BeforeClass
    public void prepareWebResource(
            @Optional("xray-test-adhoc.properties") String propertiesFileName) throws URISyntaxException {
        webResource = eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils.prepareWebResource(propertiesFileName);
        xrayConfig = XrayConfig.getInstance();
    }

    protected void setInvalidTestStatus(String testExecutionKey) throws IOException {
        final UpdateXrayTestExecution execution = new UpdateXrayTestExecution(testExecutionKey);
        final Set<XrayTestIssue> xrayTestIssues = new HashSet<>();
        final Calendar oneYearAgo = Calendar.getInstance();
        oneYearAgo.add(Calendar.YEAR, -1);
        Date invalidDate = new GregorianCalendar(1999, 12, 24).getTime();
        for (final String testKey : expectedTestResultsFullSet.keySet()) {
            xrayTestIssues.add(new XrayTestIssue(testKey, XrayTestStatus.ABORTED, invalidDate, invalidDate, null));
        }
        execution.setTests(xrayTestIssues);
        XrayUtils.syncTestExecutionReturnKey(webResource, execution);
        final JiraIssueUpdate update = JiraIssueUpdate.create()
                .field(new SetLabels(""))
                .field("versions", JiraVerb.SET, new JiraNullValue())
                .build();
        JiraUtils.updateIssue(webResource, testExecutionKey, update);
    }


    void checkTestExecutionResult(String testExecutionKey, Calendar before) throws IOException, ParseException {
        checkTestExecutionResult(testExecutionKey, expectedTestResultsFullSet, before);
    }

    void checkTestExecutionResult(String testExecutionKey, Map<String, XrayTestStatus> expectedTestResults,
                                  Calendar before) throws IOException, ParseException {

        /* get data to check */
        final JiraIssue jiraIssue = JiraUtils.getIssue(webResource, testExecutionKey, Arrays.asList(
                "summary", "description", "labels", "versions",
                xrayConfig.getRevisionFieldName(),
                xrayConfig.getTestExecutionStartTimeFieldName(),
                xrayConfig.getTestExecutionFinishTimeFieldName()));
        final Set<XrayTestIssue> testIssues = XrayUtils.getTestsFromExecution(webResource, testExecutionKey);

        /* check string fields */
        final String foundSummary = jiraIssue.getFields().get("summary").asText();
        final String foundDescription = jiraIssue.getFields().get("description").asText();
        final String foundRevision = jiraIssue.getFields().get(xrayConfig.getRevisionFieldName()).asText();
        final List<String> foundLabels = newArrayList(new ObjectMapper().treeToValue(jiraIssue.getFields().get("labels"), String[].class));

        final List<String> foundAffectedVersions = jiraIssue.getFields().get("versions").findValuesAsText("name");

        assertEquals(foundSummary, getExpectedSummary());
        assertEquals(foundDescription, getExpectedDescription());
        assertEquals(foundRevision, getExpectedRevision());

        /* check labels */
        assertTrue(foundLabels.containsAll(getExpectedLabels()), "labels not as expected");
        assertTrue(getExpectedLabels().containsAll(foundLabels), "labels not as expected");
        assertTrue(foundAffectedVersions.containsAll(getExpectedAffectedVersions()), "affectedVersions not as expected");
        assertTrue(getExpectedAffectedVersions().containsAll(foundAffectedVersions), "affectedVersions not as expected");

        /* check execution times */
        /* loosen before time since test execution times ignore seconds */
        before.add(Calendar.MINUTE, -2);
        final Date agoTime = before.getTime();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sZ", Locale.getDefault());
        final Date execStartTime = simpleDateFormat.parse(jiraIssue.getFields().findValue(xrayConfig.getTestExecutionStartTimeFieldName()).asText());
        final Date execFinishTime = simpleDateFormat.parse(jiraIssue.getFields().findValue(xrayConfig.getTestExecutionStartTimeFieldName()).asText());

        Assert.assertFalse(execFinishTime.before(execStartTime));
        assertTrue(execStartTime.after(agoTime),
                String.format("test exec start time: %s should be newer than 2 minutes ago: %s", execStartTime, before));
        assertTrue(execFinishTime.after(agoTime),
                String.format("test exec finish time: %s should be newer than 2 minutes ago: %s", execFinishTime, before));


        /* check single tests */
        for (final XrayTestIssue testIssue : testIssues) {
            final String testKey = testIssue.getTestKey();
            if (expectedTestResults.containsKey(testKey)) {
                final XrayTestStatus expectedTestStatus = expectedTestResults.get(testKey);
                assertEquals(testIssue.getStatus(), expectedTestStatus,
                        String.format("test status of %s should be %s", testKey, expectedTestStatus));

                assertTrue(testIssue.getStart().after(agoTime),
                        String.format("test start time: %s should be newer than 2 minutes ago: %s", testIssue.getStart(), agoTime));
                assertTrue(testIssue.getFinish().after(agoTime),
                        String.format("test finish time: %s should be newer than 2 minutes ago: %s", testIssue.getFinish(), agoTime));
            }
        }
    }

    protected void removeListenersFromTesterraEventService(TestNG testNG) {
        testNG.getSuiteListeners().forEach(sl -> {
            if (sl instanceof TesterraEventListener) {
                final TesterraEventListener testerraEventListener = (TesterraEventListener) sl;
                TesterraEventService.removeListener(testerraEventListener);
            }
        });
    }
}
