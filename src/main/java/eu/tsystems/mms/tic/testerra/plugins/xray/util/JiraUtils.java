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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIdReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraKeyReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssuesSearchResult;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatus;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraStatusCategory;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraTransition;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraTransitionsSearchResult;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import static java.lang.String.format;

public class JiraUtils implements Loggable {

    private static final String ATTACHMENT_PATH = "api/2/attachment";
    private static final String ISSUE_PATH = "api/2/issue";
    private static final String SEARCH_PATH = "api/2/search";
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

    public void createOrUpdateIssue(JiraIssue issue) throws IOException {
        final String string = objectMapper.writeValueAsString(issue);

        Optional<String> post;
        if (issue.hasKey()) {
            post = put(String.format("%s/%s", ISSUE_PATH, issue.getKey()), string);
        } else {
            post = post(ISSUE_PATH, string);
        }

        if (post.isPresent()) {
            JiraKeyReference jiraKeyReference = objectMapper.readValue(post.get(), JiraKeyReference.class);

            if (jiraKeyReference.hasKey()) {
                issue.setKey(jiraKeyReference.getKey());
            }
            if (jiraKeyReference.hasId()) {
                issue.setId(jiraKeyReference.getId());
            }
        }
    }

    public Optional<String> post(String apiPath, Object entity) throws IOException {
        return post(apiPath, objectMapper.writeValueAsString(entity));
    }

    public Optional<String> post(String apiPath, String body) throws IOException {
        try {
            return Optional.ofNullable(prepare(apiPath, body).post(String.class));
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() != 204) {
                unwrapException(e);
            }
        }
        return Optional.empty();
    }

    public Optional<String> put(String apiPath, Object entity) throws IOException {
        return put(apiPath, objectMapper.writeValueAsString(entity));
    }

    public Optional<String> put(String apiPath, String body) throws IOException {
        try {
            return Optional.ofNullable(prepare(apiPath, body).put(String.class));
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() != 204) {
                unwrapException(e);
            }
        }
        return Optional.empty();
    }

    private WebResource.Builder prepare(String apiPath, String body) {
        return webResource.path(apiPath)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(body);
    }

    private void unwrapException(UniformInterfaceException e) throws IOException {
        String errorMessage = e.getResponse().getEntity(String.class);
        throw new IOException(errorMessage, e);
    }

    /**
     * @param webResource
     * @param jqlQuery
     * @return returns a Jira Issue containing no fields
     * @deprecated Use {@link #searchIssues(JqlQuery)} instead
     */
    public static Set<JiraIssue> searchIssues(final WebResource webResource, final String jqlQuery) {
        return searchIssues(webResource, jqlQuery, Lists.newArrayList("\"\""));
    }

    /**
     * @deprecated Use {@link #searchIssues(JqlQuery)} instead
     */
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
            return new HashSet<>();
        }
        return jiraIssueSearchResult.getIssues();
    }

    public Stream<JiraIssue> searchIssues(JqlQuery jqlQuery) {
        return searchIssues(getWebResource(), jqlQuery.createJql()).stream();
    }

    public <T extends JiraIdReference> Stream<T> searchIssues(JqlQuery jqlQuery, Function<JiraIssue, T> issueSupplier) {
        return searchIssues(jqlQuery).map(issueSupplier);
    }


    /**
     * @deprecated Use {@link #getAvailableTransitions(String)} instead
     */
    public static Set<JiraTransition> getTransitions(final WebResource webResource,
                                                     final String issueKey) throws IOException {
        JiraUtils jiraUtils = new JiraUtils(webResource);
        return jiraUtils.getAvailableTransitions(issueKey);
    }

    public Set<JiraTransition> getAvailableTransitions(String issueKey) throws IOException {
        final String result = webResource.path(format("%s/%s/transitions", ISSUE_PATH, issueKey)).get(String.class);
        JiraTransitionsSearchResult jiraTransitionsSearchResult = objectMapper.readValue(result, JiraTransitionsSearchResult.class);
        return jiraTransitionsSearchResult.getTransitions();
    }

    /**
     * @deprecated Use {@link #performTransition(String, JiraTransition)} instead
     */
    public static void doTransitionById(final WebResource webResource, final String issueKey,
                                        final int transitionId) throws IOException {
        JiraUtils jiraUtils = new JiraUtils(webResource);
        jiraUtils.performTransition(issueKey, new JiraTransition(Integer.toString(transitionId)));
    }

    public void performTransition(String issueKey, JiraTransition jiraTransition) throws IOException {
        ObjectMapper objectMapper = this.objectMapper.copy();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        post(format("%s/%s/transitions", ISSUE_PATH, issueKey), objectMapper.writeValueAsString(jiraTransition));
    }

    public Optional<JiraTransition> getTransitionByStatusCategory(Set<JiraTransition> jiraTransitions, JiraStatusCategory statusCategory) {
        return jiraTransitions.stream()
                .filter(jiraTransition -> {
                    JiraStatus to = jiraTransition.getTo();
                    if (to != null) {
                        JiraStatusCategory sc = to.getStatusCategory();
                        if (sc != null) {
                            if (sc.hasKey()) {
                                return sc.getKey().equals(statusCategory.getKey());
                            }
                        }
                    }
                    return false;
                })
                .findFirst();
    }

    /**
     * @deprecated Use {@link #performTransition(String, JiraTransition)} instead
     */
    public static void doTransitionByName(final WebResource webResource, final String issueKey,
                                          final String transitionName) throws IOException {
        final Set<JiraTransition> transitions = getTransitions(webResource, issueKey);
        for (final JiraTransition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                doTransitionById(webResource, issueKey, Integer.parseInt(transition.getId()));
                return;
            }
        }
        throw new RuntimeException(String.format("Transition not available: %s", transitionName));
    }

    /**
     * @deprecated Use {@link #getIssue(String)} instead
     */
    public static JiraStatus getIssueStatus(final WebResource webResource, final String issueKey) throws IOException {
        JiraUtils jiraUtils = new JiraUtils(webResource);
        JiraIssue issue = jiraUtils.getIssue(issueKey);
        return issue.getStatus();
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

    protected WebResource getWebResource() {
        return webResource;
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
