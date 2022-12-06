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

package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueTypeEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.ProjectEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.RevisionContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.SummaryContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatusCategory;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.ExecutionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import org.testng.ITestClass;
import org.testng.ITestResult;

import java.util.LinkedList;

public interface XrayMapper {
    String PROPERTY_TEST_SET_TESTS = "xray.test.set.tests.field.id";
    String PROPERTY_TEST_EXECUTION_START_DATE = "xray.test.execution.start.time.field.id";
    String PROPERTY_TEST_EXECUTION_FINISH_DATE = "xray.test.execution.finish.time.field.id";
    String PROPERTY_TEST_EXECUTION_REVISION = "xray.test.execution.revision.field.id";
    String PROPERTY_TEST_EXECUTION_TEST_ENVIRONMENTS = "xray.test.execution.test-environments.field.id";
    String PROPERTY_TEST_EXECUTION_TEST_PLANS = "xray.test.execution.test-plans.field.id";
    String PROPERTY_TEST_EXECUTION_ISSUETYPE_NAME = "xray.test.execution.issuetype.name";
    String PROPERTY_TEST_SET_ISSUETYPE_NAME = "xray.test.set.issuetype.name";
    String PROPERTY_TEST_ISSUETYPE_NAME = "xray.test.issuetype.name";

    /**
     * called for matching Test method against Xray Test
     * create a Jql-Query that is able to retrieve a single Xray-Test
     * 'project = $projektKey AND issuetype = Test AND key in testSetTests($testSetKey)' is prepended automatically
     * return null if no automatic matching is required
     *
     * @param testNgResult the test result
     * @return JqlQuery that is able to match a single Xray-Test or null
     * @deprecated Use {@link #queryTest(MethodContext)}
     */
    default JqlQuery resultToXrayTest(ITestResult testNgResult) {
        return null;
    }

    /**
     * Creates a {@link JqlQuery} for mapping {@link MethodContext} to a JiraTest
     */
    default JqlQuery queryTest(MethodContext methodContext) {
        return methodContext.getTestNgResult().map(this::resultToXrayTest).orElse(null);
    }

    /**
     * If true, try to create or update a Xray Test
     * <p>
     * The create/update process includes issue attributes and test steps.
     */
    default boolean shouldCreateNewTest(MethodContext methodContext) {
        return false;
    }

    /**
     * called for matching Test class against Xray TestSet
     * create a Jql-Query that is able to retrieve a single Xray-TestSet
     * 'project = $projektKey AND issuetype = "Test Set"' is prepended automatically
     * return null if no automatic matching is required
     *
     * @param testNgClass test class
     * @return JqlQuery that is able to match a single Xray-TestSet or null
     * @deprecated Use {@link #queryTestSet(ClassContext)} instead
     */
    default JqlQuery classToXrayTestSet(ITestClass testNgClass) {
        return null;
    }

    default JqlQuery queryTestSet(ClassContext classContext) {
        return classContext.readMethodContexts()
                .findFirst()
                .flatMap(MethodContext::getTestNgResult)
                .map(testResult -> classToXrayTestSet(testResult.getMethod().getTestClass()))
                .orElse(null);
    }

    /**
     * If true, try to create a Xray Test Set
     */
    default boolean shouldCreateNewTestSet(ClassContext classContext) {
        return false;
    }

    default JqlQuery queryTestExecution(XrayTestExecutionIssue xrayTestExecutionIssue) {
        return JqlQuery.create()
                .addCondition(new ProjectEquals(xrayTestExecutionIssue.getProject().getKey()))
                .addCondition(new IssueTypeEquals(xrayTestExecutionIssue.getIssueType()))
                .addCondition(new SummaryContainsExact(xrayTestExecutionIssue.getSummary()))
                .addCondition(new RevisionContainsExact(xrayTestExecutionIssue.getRevision()))
                .build();
    }

    /**
     * Gets called when the test execution is created or updated.
     */
    default void updateTestExecution(XrayTestExecutionIssue xrayTestExecutionIssue, ExecutionContext executionContext) {
    }

    /**
     * Gets called every time when a test is assigned to the test set.
     */
    default void updateTestSet(XrayTestSetIssue xrayTestSetIssue, ClassContext classContext) {
    }

    /**
     * Gets called every time a test will be synchronized.
     */
    default void updateTest(XrayTestIssue xrayTestIssue, MethodContext methodContext) {
    }

    /**
     * Returns the default test issue summery for creating new tests or searching for mapping
     */
    default String getDefaultTestIssueSummery(MethodContext methodContext) {
        return methodContext.getName();
    }

    /**
     * Define the order of transitions to close a Xray test execution beginning in status 'NEW':
     * 'Ready for test' (category 'indeterminate')
     * 'In test' (category 'indeterminate')
     * 'Resolved' (category 'done')
     *
     * Because the name of the status can change, you only set up the categories of transitions. Xray connector checks which transition is
     * possible from the current status and select the next transition with needed category.
     */
    default LinkedList<JiraStatusCategory> getTestExecutionTransitions() {
        LinkedList<JiraStatusCategory> list = new LinkedList<>();
        list.add(JiraStatusCategory.INDETERMINATE);
        list.add(JiraStatusCategory.INDETERMINATE);
        list.add(JiraStatusCategory.DONE);
        return list;
    }
}
