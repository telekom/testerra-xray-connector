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
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.ExplicitJiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.JiraUtils;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.utils.ProxyUtils;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class XrayConnector implements Loggable {

    private final WebResource webResource;

    public XrayConnector() {
        final Client client;

        if (ProxyUtils.getSystemHttpsProxyUrl() != null) {
            client = RESTClientFactory.createWithProxy(ProxyUtils.getSystemHttpsProxyUrl());
        } else {
            client = RESTClientFactory.createDefault();
        }

        XrayConfig xrayConfig = XrayConfig.getInstance();
        webResource = client.resource(xrayConfig.getRestServiceUri());

        if (StringUtils.isNotEmpty(xrayConfig.getToken())) {
            log().info("Use Bearer token authentication");
            webResource.addFilter(new HttpBearerTokenAuthFilter(xrayConfig.getToken()));
        } else {
            log().info("Use Basic authentication");
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

    public void uploadTestExecutionAttachment(final String issueKey, final InputStream is, final String fileName) {
        JiraUtils.uploadAttachment(webResource, issueKey, is, fileName);
    }

    public WebResource getWebResource() {
        return this.webResource;
    }
}
