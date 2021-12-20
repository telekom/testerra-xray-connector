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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Field;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraFieldsSearchResult;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssueKeyReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssuesSearchResult;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatus;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraTransition;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraTransitionsSearchResult;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import static java.lang.String.format;


public final class JiraUtils implements Loggable {

    private static final String ATTACHMENT_PATH = "api/2/attachment";
    private static final String ISSUE_PATH = "api/2/issue";
    private static final String SEARCH_PATH = "api/2/search";
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final WebResource webResource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JiraUtils(WebResource webResource) {
        this.webResource = webResource;
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    /**
     * @deprecated Use {@link #getIssue(String)} instead
     * @param webResource
     * @param issueKey
     * @return
     * @throws IOException
     */
    public static JiraIssue getIssue(final WebResource webResource, final String issueKey) throws IOException {
        return getIssue(webResource, issueKey, Lists.newArrayList("\"\""));
    }

    public JiraIssue getIssue(String issueKey) throws IOException {
        try {
            return getIssue(this.webResource, issueKey, Lists.newArrayList());
        } catch (UniformInterfaceException e) {
            unwrapException(e);
        }
        return new JiraIssue();
    }

    /**
     * @deprecated Use {@link #getIssue(WebResource, String)} instead
     */
    public static JiraIssue getIssue(
            final WebResource webResource,
            final String issueKey,
            final Collection<String> fields
    ) throws IOException {
        JiraUtils jiraUtils = new JiraUtils(webResource);
        WebResource path = webResource.path(format("%s/%s", ISSUE_PATH, issueKey));
        if (!fields.isEmpty()) {
            path = path.queryParam("fields", StringUtils.join(fields, ','));
        }
        String result = path.get(String.class);
        return jiraUtils.objectMapper.readValue(result, JiraIssue.class);
    }

    /**
     * @deprecated Use {@link #createOrUpdateIssue(JiraIssue)}
     * @param webResource
     * @param issueKey
     * @param update
     * @throws JsonProcessingException
     */
    public static void updateIssue(final WebResource webResource, final String issueKey,
                                   final JiraIssueUpdate update) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final String string = objectMapper.writeValueAsString(update);
        webResource.path(format("%s/%s", ISSUE_PATH, issueKey))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(string);
    }

    public void createOrUpdateIssue(JiraIssue issue) throws IOException {
        final String string = objectMapper.writeValueAsString(issue);
        String resourcePath;
        try {
            String responseJson;
            if (issue.hasKey()) {
                resourcePath = String.format("%s/%s", ISSUE_PATH, issue.getKey());
                responseJson = webResource.path(resourcePath)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .put(String.class, string);
            } else {
                resourcePath = ISSUE_PATH;
                responseJson = webResource.path(resourcePath)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(String.class, string);

            }
            JiraIssueKeyReference jiraIssueKeyReference = objectMapper.readValue(responseJson, JiraIssueKeyReference.class);
            issue.setKey(jiraIssueKeyReference.getKey());
            issue.setId(jiraIssueKeyReference.getId());

        } catch (UniformInterfaceException e) {
            unwrapException(e);
        }
    }

    private void unwrapException(UniformInterfaceException e) throws IOException {
        String errorMessage = e.getResponse().getEntity(String.class);
//        String errorMessage = new BufferedReader(new InputStreamReader(e.getResponse().getEntityInputStream(), StandardCharsets.UTF_8))
//                .lines()
//                .collect(Collectors.joining("\n"));
        throw new IOException(errorMessage, e);
    }

    /**
     * @param webResource
     * @param jqlQuery
     * @return returns a Jira Issue containing no fields
     */
    public static Set<JiraIssue> searchIssues(final WebResource webResource, final String jqlQuery) {
        return searchIssues(webResource, jqlQuery, Lists.newArrayList("\"\""));
    }

    public static Set<JiraIssue> searchIssues(final WebResource webResource, final String jqlQuery,
                                              final Collection<String> fields) {
        final String result = webResource.path(SEARCH_PATH)
                .queryParam("validateQuery", "true")
                .queryParam("jql", jqlQuery)
                .queryParam("fields", StringUtils.join(fields, ','))
                .get(String.class);
        final ObjectMapper objectMapper = new ObjectMapper();
        final JiraIssuesSearchResult jiraIssueSearchResult;
        try {
            jiraIssueSearchResult = objectMapper.readValue(result, JiraIssuesSearchResult.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return jiraIssueSearchResult.getIssues();
    }

    public static Set<JiraTransition> getTransitions(final WebResource webResource,
                                                     final String issueKey) throws IOException {
        final String result = webResource.path(format("%s/%s/transitions", ISSUE_PATH, issueKey)).get(String.class);
        final ObjectMapper objectMapper = new ObjectMapper();
        final JiraTransitionsSearchResult transitionsSearchResult = objectMapper.readValue(result, JiraTransitionsSearchResult.class);
        return transitionsSearchResult.getTransitions();
    }

    public static void doTransitionById(final WebResource webResource, final String issueKey,
                                        final int transitionId) throws IOException {
        final JiraTransition transition = new JiraTransition();
        transition.setId(transitionId);

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        final String string = objectMapper.writeValueAsString(transition);
        webResource.path(format("%s/%s/transitions", ISSUE_PATH, issueKey))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(string);
    }

    public static void doTransitionByName(final WebResource webResource, final String issueKey,
                                          final String transitionName) throws IOException {
        final Set<JiraTransition> transitions = getTransitions(webResource, issueKey);
        for (final JiraTransition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                doTransitionById(webResource, issueKey, transition.getId());
                return;
            }
        }
    }

    public static JiraStatus getIssueStatus(final WebResource webResource, final String issueKey) throws IOException {
        final String result = webResource.path(format("%s/%s", ISSUE_PATH, issueKey)).queryParam("fields", "status").get(String.class);
        final ObjectMapper objectMapper = new ObjectMapper();
        final JiraFieldsSearchResult jiraFieldsSearchResult = objectMapper.readValue(result, JiraFieldsSearchResult.class);
        return jiraFieldsSearchResult.getFields().getStatus();
    }

    public static void uploadJsonAttachment(final WebResource webResource, final String issueKey, final String content,
                                            final String displayedFileName) {
        final FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final FormDataContentDisposition dispo = FormDataContentDisposition.name("file").fileName(displayedFileName).build();
        final FormDataBodyPart bodyPart = new FormDataBodyPart(dispo, content, MediaType.APPLICATION_JSON_TYPE);
        formDataMultiPart.bodyPart(bodyPart);

        webResource.path(format("%s/%s/attachments", ISSUE_PATH, issueKey))
                .header("X-Atlassian-Token", "nocheck")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(String.class, formDataMultiPart);
    }

    public static void uploadAttachment(final WebResource webResource, final String issueKey, final InputStream is,
                                        final String displayedFileName) {
        final FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final FormDataContentDisposition dispo = FormDataContentDisposition.name("file").fileName(displayedFileName).build();
        final FormDataBodyPart bodyPart = new FormDataBodyPart(dispo, is, MediaType.MULTIPART_FORM_DATA_TYPE);
        formDataMultiPart.bodyPart(bodyPart);

        webResource.path(format("%s/%s/attachments", ISSUE_PATH, issueKey))
                .header("X-Atlassian-Token", "nocheck")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(formDataMultiPart);
    }

    public static void deleteAllAttachments(final WebResource webResource, final String issueKey) throws IOException {
        final JiraIssue issue = JiraUtils.getIssue(webResource, issueKey, Lists.newArrayList("attachment"));
        final List<String> attachmentIds = issue.getFields().findValue("attachment").findValuesAsText("id");
        for (final String id : attachmentIds) {
            webResource.path(format("%s/%s", ATTACHMENT_PATH, id))
                    .delete();
        }
    }

    public static String createTestExecutionGeneric(final WebResource webResource,
                                                    XrayInfo xrayInfo) throws IOException {

        final JiraIssue jiraIssue = new JiraIssue();
        final ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        final ObjectNode fieldsNode = om.createObjectNode();



        fieldsNode.put("issuetype", om.createObjectNode().put("name", IssueType.TestExecution.toString()));
        fieldsNode.put("project", om.createObjectNode().put("key", xrayInfo.getProject()));
        fieldsNode.put("summary", xrayInfo.getSummary());
        fieldsNode.put("description", xrayInfo.getDescription());
        if (xrayInfo.getVersion() != null) {
            fieldsNode.put("fixVersions", om.createArrayNode().add(om.createObjectNode().put("name", xrayInfo.getVersion())));
        }
        if (xrayInfo.getUser() != null) {
            fieldsNode.put("assignee", om.createObjectNode().put("name", xrayInfo.getUser()));
        }
        fieldsNode.put(Fields.REVISION.getFieldName(), xrayInfo.getRevision());
        fieldsNode.put(Fields.TEST_EXECUTION_START_DATE.getFieldName(), dateFormat.format(new Date()));
        final ArrayNode arrayNode = om.createArrayNode();
        xrayInfo.readTestEnvironments().forEach(arrayNode::add);
        fieldsNode.put(Fields.TEST_ENVIRONMENTS.getFieldName(), arrayNode);
        jiraIssue.setFields(fieldsNode);
        final String entity = om.writeValueAsString(jiraIssue);

        final String response = webResource.path(ISSUE_PATH)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(entity)
                .post(String.class);

        if (response.isEmpty()) {
            // avoid NPE on findValue in next line, when response is empty.
            return response;
        }

        return om.readTree(response).findValue("key").asText();
    }
}
