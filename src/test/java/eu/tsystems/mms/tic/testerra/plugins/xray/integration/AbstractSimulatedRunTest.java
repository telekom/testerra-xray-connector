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

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.hook.XrayConnectorHook;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.synchronizer.SimulatedTestRunXrayResultsSynchronizer;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.JiraUtils;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public abstract class AbstractSimulatedRunTest extends TesterraTest {

    protected Map<String, XrayTestExecutionImport.TestRun.Status> fullWithoutParametrized = ImmutableMap.of(
            "SWFTE-4", XrayTestExecutionImport.TestRun.Status.PASS,
            "SWFTE-5", XrayTestExecutionImport.TestRun.Status.FAIL,
            "SWFTE-6", XrayTestExecutionImport.TestRun.Status.SKIPPED
    );
    private WebResource webResource;
    private XrayConfig xrayConfig;
    private Map<String, XrayTestExecutionImport.TestRun.Status> expectedTestResultsFullSet = ImmutableMap.of(
            "SWFTE-4", XrayTestExecutionImport.TestRun.Status.PASS,
            "SWFTE-5", XrayTestExecutionImport.TestRun.Status.FAIL,
            "SWFTE-6", XrayTestExecutionImport.TestRun.Status.SKIPPED,
            "SWFTE-319", XrayTestExecutionImport.TestRun.Status.PASS,
            "SWFTE-320", XrayTestExecutionImport.TestRun.Status.FAIL
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
    public void prepareWebResource(@Optional("sync.test.properties") String propertiesFileName) throws URISyntaxException {
        webResource = eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils.prepareWebResource(propertiesFileName);
        xrayConfig = XrayConfig.getInstance();
        XrayConnectorHook.getInstance().setXrayResultsSynchronizer(new SimulatedTestRunXrayResultsSynchronizer());
    }

    protected void setInvalidTestStatus(String testExecutionKey) throws IOException {
        final XrayTestExecutionImport execution = new XrayTestExecutionImport(testExecutionKey);
        final Set<XrayTestExecutionImport.TestRun> xrayTestIssues = new HashSet<>();
        final Calendar oneYearAgo = Calendar.getInstance();
        oneYearAgo.add(Calendar.YEAR, -1);
        Date invalidDate = new GregorianCalendar(1999, 12, 24).getTime();
        for (final String testKey : expectedTestResultsFullSet.keySet()) {
            XrayTestExecutionImport.TestRun testRun = new XrayTestExecutionImport.TestRun(testKey);
            testRun.setStatus(XrayTestExecutionImport.TestRun.Status.ABORTED);
            testRun.setStart(invalidDate);
            testRun.setFinish(invalidDate);
            xrayTestIssues.add(testRun);
        }
        execution.setTests(xrayTestIssues);

        XrayUtils xrayUtils = new XrayUtils(webResource);
        xrayUtils.importTestExecution(execution);
    }


    void checkTestExecutionResult(String testExecutionKey, Calendar before) throws IOException, ParseException {
        checkTestExecutionResult(testExecutionKey, expectedTestResultsFullSet, before);
    }

    void checkTestExecutionResult(String testExecutionKey, Map<String, XrayTestExecutionImport.TestRun.Status> expectedTestResults,
                                  Calendar before) throws IOException, ParseException {

        /* get data to check */
        final JiraIssue rawIssue = JiraUtils.getIssue(webResource, testExecutionKey, Arrays.asList(
                "summary", "description", "labels", "versions",
                Fields.TEST_EXECUTION_REVISION.getFieldName(),
                Fields.TEST_EXECUTION_START_DATE.getFieldName(),
                Fields.TEST_EXECUTION_FINISH_DATE.getFieldName()
        ));

        XrayUtils xrayUtils = new XrayUtils(webResource);

        XrayTestExecutionIssue jiraIssue = new XrayTestExecutionIssue(rawIssue);
        final Set<XrayTestExecutionImport.TestRun> testRuns = xrayUtils.getTestRunsByTestExecutionKey(testExecutionKey);

        /* check string fields */
        final String foundSummary = jiraIssue.getSummary();
        final String foundDescription = jiraIssue.getDescription();
        final String foundRevision = jiraIssue.getRevision();
        final List<String> foundLabels = jiraIssue.getLabels();

        final List<String> foundAffectedVersions = jiraIssue.getVersions().stream().map(JiraNameReference::getName).collect(Collectors.toList());

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
        final Date execStartTime = jiraIssue.getStartDate();
        final Date execFinishTime = jiraIssue.getFinishDate();

        Assert.assertFalse(execFinishTime.before(execStartTime));
        assertTrue(execStartTime.after(agoTime),
                String.format("test exec start time: %s should be newer than 2 minutes ago: %s", execStartTime, before));
        assertTrue(execFinishTime.after(agoTime),
                String.format("test exec finish time: %s should be newer than 2 minutes ago: %s", execFinishTime, before));


        /* check single tests */
        for (final XrayTestExecutionImport.TestRun testRunIssue : testRuns) {
            final String testKey = testRunIssue.getTestKey();
            if (expectedTestResults.containsKey(testKey)) {
                final XrayTestExecutionImport.TestRun.Status expectedTestStatus = expectedTestResults.get(testKey);
                assertEquals(testRunIssue.getStatus(), expectedTestStatus,
                        String.format("test status of %s should be %s", testKey, expectedTestStatus));

                assertTrue(testRunIssue.getStart().after(agoTime),
                        String.format("test start time: %s should be newer than 2 minutes ago: %s", testRunIssue.getStart(), agoTime));
                assertTrue(testRunIssue.getFinish().after(agoTime),
                        String.format("test finish time: %s should be newer than 2 minutes ago: %s", testRunIssue.getFinish(), agoTime));
            }
        }
    }

}
