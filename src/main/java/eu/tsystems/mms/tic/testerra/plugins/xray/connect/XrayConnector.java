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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.filter.GetRequestOnlyFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.filter.LoggingFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueTypeEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.KeyInTestSetTests;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.ProjectEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.RevisionContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.SummaryContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.ExplicitJiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.NotSyncableException;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.JiraUtils;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import eu.tsystems.mms.tic.testframework.utils.ProxyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XrayConnector {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final WebResource webResource;
    private final XrayConfig xrayConfig;
    private final XrayInfo xrayInfo;

    public XrayConnector(final XrayInfo xrayInfo) {

        this.xrayInfo = xrayInfo;

        final Client client;

        if (ProxyUtils.getSystemHttpsProxyUrl() != null) {
            client = RESTClientFactory.createWithProxy(ProxyUtils.getSystemHttpsProxyUrl());
        } else {
            client = RESTClientFactory.createDefault();
        }

        xrayConfig = XrayConfig.getInstance();
        webResource = client.resource(xrayConfig.getRestServiceUri());

        if (StringUtils.isNotEmpty(xrayConfig.getToken())) {
            logger.info("Use Bearer token authentication");
            webResource.addFilter(new HttpBearerTokenAuthFilter(xrayConfig.getToken()));
        } else {
            logger.info("Use Basic authentication");
            webResource.addFilter(new HTTPBasicAuthFilter(xrayConfig.getUsername(), xrayConfig.getPassword()));
        }

        if (xrayConfig.isWebResourceFilterGetRequestsOnlyEnabled()) {
            webResource.addFilter(new GetRequestOnlyFilter());
        }

        if (xrayConfig.isWebResourceFilterLoggingEnabled()) {
            webResource.addFilter(new LoggingFilter());
        }

    }

    @Deprecated
    public List<String> findTestKeys(final String testSetKey, final JqlQuery methodReferenceQuery) {
        final JqlQuery baseQuery = JqlQuery.create()
                .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                .addCondition(new IssueTypeEquals(IssueType.Test))
                .addCondition(new KeyInTestSetTests(testSetKey))
                .build();
        final JqlQuery jqlQuery = JqlQuery.combine(baseQuery, methodReferenceQuery);

        final Set<JiraIssue> foundJiraIssues = JiraUtils.searchIssues(webResource, jqlQuery.createJql());
        return foundJiraIssues.stream().map(JiraIssue::getKey).collect(Collectors.toList());
    }

    @Deprecated
    public Set<String> searchForExistingTestExecutions() throws IOException {
        final JqlQuery jqlQuery = JqlQuery.create()
                .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                .addCondition(new IssueTypeEquals(IssueType.TestExecution))
                .addCondition(new SummaryContainsExact(xrayInfo.getSummary()))
                .addCondition(new RevisionContainsExact(xrayInfo.getRevision()))
                .build();
        final Set<JiraIssue> foundIssues = JiraUtils.searchIssues(webResource, jqlQuery.createJql());
        return foundIssues.stream().map(JiraIssue::getKey).collect(Collectors.toSet());
    }

    @Deprecated
    public synchronized String findOrCreateTestExecution(final Collection<String> testKeys) throws IOException, NotSyncableException {
        return findOrCreateTestExecution(testKeys, null, null);
    }

    @Deprecated
    public synchronized String findOrCreateTestExecution(final Collection<String> testKeys, JiraIssueUpdate onCreate, JiraIssueUpdate onUpdate) throws
            IOException, NotSyncableException {
        final Set<String> foundTestExecutionKeys = searchForExistingTestExecutions();

        if (foundTestExecutionKeys.size() == 1) {
            /* one hit */
            final String testExecKey = foundTestExecutionKeys.iterator().next();
            return prepareTestExecutionUpdate(testKeys, testExecKey, onUpdate);

        } else if (foundTestExecutionKeys.isEmpty()) {
            /* no hit */
            return prepareTestExecutionCreation(testKeys, onCreate);

        } else {
            throw new NotSyncableException("must find at maximum one match for jql query");
        }
    }

    @Deprecated
    private String prepareTestExecutionCreation(final Iterable<String> testKeys, JiraIssueUpdate jiraIssueUpdate)
            throws IOException, NotSyncableException {
        //        final String testExecKey = JiraUtils.createTestExecutionGeneric(webResource, xrayConfig.getProjectKey(), summary, description, revision);
        final String testExecKey;
        if (testKeys != null) {
            final XrayTestExecutionImport freshTestExecution = XrayUtils.createFreshTestExecution(xrayInfo, testKeys);
            testExecKey = XrayUtils.syncTestExecutionReturnKey(webResource, freshTestExecution);
        } else {
            //            testExecKey = JiraUtils.createTestExecutionGeneric(webResource, xrayInfo.getProject(), xrayInfo.getSummary(), xrayInfo.getDescription(), xrayInfo.getRevision());
            testExecKey = JiraUtils.createTestExecutionGeneric(webResource, xrayInfo);
        }
        logger.info("New test execution id: {}", testExecKey);

        /* on new transitions */
        doTransitions(testExecKey, xrayConfig.getTransitionsOnCreated());

        /* on new jira issue update */
        if (jiraIssueUpdate != null) {
            updateIssue(testExecKey, jiraIssueUpdate);
        }
        return testExecKey;
    }

    private String prepareTestExecutionUpdate(final Collection<String> testKeys, final String testExecKey, JiraIssueUpdate jiraUpdate) throws
            IOException {
        logger.info("Updating test execution {}", testExecKey);

        /* attaching previous results */
        final String previousResultsFilename = xrayConfig.getPreviousResultsFilename();
        if ((previousResultsFilename != null) && !previousResultsFilename.equals("")) {
            final String previousResultJson = XrayUtils.exportTestExecutionAsJson(webResource, testExecKey);
            JiraUtils.uploadJsonAttachment(webResource, testExecKey, previousResultJson, previousResultsFilename);
        }

        if (testKeys != null) {
            /* set all tests to _todo status */
            final XrayTestExecutionImport testExecution = XrayUtils.createUpdateTestExecution(testExecKey, testKeys);
            XrayUtils.syncTestExecutionReturnKey(webResource, testExecution);
        }

        /* on update transitions */
        doTransitions(testExecKey, xrayConfig.getTransitionsOnUpdated());

        /* on update jira issue update */
        if (jiraUpdate != null) {
            updateIssue(testExecKey, jiraUpdate);
        }
        return testExecKey;
    }

    public void doTransitions(final String testExecutionKey, final Collection<String> transitionNames) {
        for (final String transitionName : transitionNames) {
            try {
                JiraUtils.doTransitionByName(webResource, testExecutionKey, transitionName);
            } catch (final IOException e) {
                logger.error("Could not do transition {}", transitionName);
            }
        }
    }

    public void uploadTestExecutionAttachment(final String issueKey, final InputStream is, final String fileName) {
        JiraUtils.uploadAttachment(webResource, issueKey, is, fileName);
    }

    public void addTestExecutionComment(final String testExecutionKey, final String comment) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();

        final ObjectNode commentNode = objectMapper.createObjectNode();
        final ObjectNode addNode = objectMapper.createObjectNode();
        final ArrayNode addArrayNode = objectMapper.createArrayNode();
        final ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("body", comment);
        addNode.put("add", bodyNode);
        addArrayNode.add(addNode);
        commentNode.put("comment", addArrayNode);

        final ExplicitJiraIssueUpdate update = new ExplicitJiraIssueUpdate();
        update.setUpdate(commentNode);
        JiraUtils.updateIssue(webResource, testExecutionKey, update);
    }

    @Deprecated
    public List<String> findTestSetKeys(final JqlQuery classReferenceQuery) {
        return findTestSets(classReferenceQuery).map(JiraIssue::getKey).collect(Collectors.toList());
    }

    @Deprecated
    public Stream<XrayTestSetIssue> findTestSets(final JqlQuery classReferenceQuery) {
        final JqlQuery baseQuery = JqlQuery.create()
                .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                .addCondition(new IssueTypeEquals(IssueType.TestSet))
                .build();
        final JqlQuery jqlQuery = JqlQuery.combine(baseQuery, classReferenceQuery);
        return JiraUtils.searchIssues(webResource, jqlQuery.createJql())
                .stream()
                .map(XrayTestSetIssue::new);
    }

    @Deprecated
    public void updateIssue(String testExecutionKey, JiraIssueUpdate jiraIssueUpdate) throws JsonProcessingException {
        JiraUtils.updateIssue(webResource, testExecutionKey, jiraIssueUpdate);
    }

    public WebResource getWebResource() {
        return this.webResource;
    }
}
