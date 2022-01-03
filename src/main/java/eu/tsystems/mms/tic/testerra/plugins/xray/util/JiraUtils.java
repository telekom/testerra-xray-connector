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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraFieldsSearchResult;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIdReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraKeyReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssuesSearchResult;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatus;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraTransition;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraTransitionsSearchResult;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import static java.lang.String.format;


public class JiraUtils implements Loggable {

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
        return new JiraUtils(webResource).getIssue(issueKey);
    }

    public JiraIssue getIssue(String issueKey) throws IOException {
        try {
            return getIssue(this.webResource, issueKey, Lists.newArrayList());
        } catch (UniformInterfaceException e) {
            unwrapException(e);
        }
        return new JiraIssue();
    }

    public <T extends JiraIdReference> T getIssue(String issueKey, Function<JiraIssue, T> issueSupplier) throws IOException {
        return issueSupplier.apply(getIssue(issueKey));
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
        try {
            JiraKeyReference jiraIssueKeyReference;
            if (issue.hasKey()) {
                jiraIssueKeyReference = put(String.format("%s/%s", ISSUE_PATH, issue.getKey()), string);
            } else {
                jiraIssueKeyReference = post(ISSUE_PATH, string);
            }

            if (jiraIssueKeyReference.hasKey()) {
                issue.setKey(jiraIssueKeyReference.getKey());
            }
            if (jiraIssueKeyReference.hasId()) {
                issue.setId(jiraIssueKeyReference.getId());
            }

        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() != 204) {
                unwrapException(e);
            }
        }
    }

    public JiraKeyReference post(String apiPath, String body) {
        String jsonResponse = webResource.path(apiPath)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, body);
        return responseToKey(jsonResponse);
    }

    public JiraKeyReference put(String apiPath, String body) {
        String jsonResponse = webResource.path(apiPath)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(String.class, body);
        return responseToKey(jsonResponse);
    }

    private JiraKeyReference responseToKey(String jsonResponse) {
        try {
            return objectMapper.readValue(jsonResponse, JiraKeyReference.class);
        } catch (IOException e) {
            log().error("Unable to parse response to key reference", e);
            return new JiraKeyReference();
        }
    }

    private void unwrapException(UniformInterfaceException e) throws IOException {
        String errorMessage = e.getResponse().getEntity(String.class);
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
        JiraUtils jiraUtils = new JiraUtils(webResource);
        final String result = webResource.path(SEARCH_PATH)
                .queryParam("validateQuery", "true")
                .queryParam("jql", jqlQuery)
                .queryParam("fields", StringUtils.join(fields, ','))
                .get(String.class);
        final JiraIssuesSearchResult jiraIssueSearchResult;
        try {
            jiraIssueSearchResult = jiraUtils.objectMapper.readValue(result, JiraIssuesSearchResult.class);
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
        issue.getAttachments().forEach(issueRef -> {
            webResource.path(format("%s/%s", ATTACHMENT_PATH, issueRef.getId()))
                    .delete();
        });
    }

    /**
     * @deprecated Use {@link #createOrUpdateIssue(JiraIssue)} instead
     */
    public static String createTestExecutionGeneric(WebResource webResource, XrayTestExecutionIssue xrayInfo) throws IOException {
        JiraUtils jiraUtils = new JiraUtils(webResource);
        jiraUtils.createOrUpdateIssue(xrayInfo);
        return xrayInfo.getKey();
    }

    protected static DateFormat getDateFormat() {
        return dateFormat;
    }

    protected WebResource getWebResource() {
        return webResource;
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
