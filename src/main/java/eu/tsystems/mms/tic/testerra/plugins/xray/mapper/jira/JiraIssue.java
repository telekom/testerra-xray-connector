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

package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


public class JiraIssue extends JiraIssueKeyReference implements Loggable {
    private Map<String, Object> fields;

    public JiraIssue() {
        this.fields = new HashMap<>();
    }

    public JiraIssue(String key) {
        this();
        this.setKey(key);
    }

    public JiraIssue(JiraIssue issue) {
        this.setKey(issue.getKey());
        this.fields = issue.fields;
    }

    @Deprecated
    public JsonNode getFields() {
        ObjectMapper om = new ObjectMapper();
        return om.valueToTree(this.fields);
    }

    @Deprecated
    public void setFields(final JsonNode fields) {
        ObjectMapper om = new ObjectMapper();
        try {
            this.fields = om.treeToValue(fields, Map.class);
        } catch (JsonProcessingException e) {
           log().error("Unable to set fields", e);
        }
    }

    private <T extends Object> T getOrCreateField(String name, Supplier<T> fieldSupplier) {
        T val;
        if (!this.fields.containsKey(name)) {
            val = fieldSupplier.get();
            this.fields.put(name, val);
            return val;
        }
        try {
            val = (T)this.fields.get(name);
        } catch (ClassCastException e) {
            val = fieldSupplier.get();
            this.fields.put(name, val);
        }
        return val;
    }
//
//    private <T extends Object> Optional<T> getMappableField(String name, Class<T> targetClass) {
//        if (!this.fields.containsKey(name)) {
//            return Optional.empty();
//        }
//
//        Object val = this.fields.get(name);
//        if (!targetClass.isInstance(val)) {
//            return Optional.empty();
//        } else {
//            return Optional.of((T)val);
//        }
//    }

    @JsonIgnore
    public Set<String> getLabels() {
        return (Set<String>)this.fields.getOrDefault("labels", new HashSet<String>());
    }

    @JsonIgnore
    public void setLabels(final Set<String> labels) {
        this.fields.put("labels", labels);
    }

    @JsonIgnore
    public String getSummary() {
        return (String)this.fields.getOrDefault("summary", "");
    }

    @JsonIgnore
    public void setSummary(final String summary) {
        this.fields.put("summary", summary);
    }

    @JsonIgnore
    public JiraStatus getStatus() {
        return getOrCreateField("status", JiraStatus::new);
    }

    @JsonIgnore
    public void setStatus(final JiraStatus status) {
        this.fields.put("status", status);
    }

    @JsonIgnore
    public JiraIssueKeyReference getProject() {
        return getOrCreateField("project", JiraIssueKeyReference::new);
    }

    @JsonIgnore
    public void setProject(JiraIssueKeyReference project) {
        this.fields.put("project", project);
    }

    @JsonIgnore
    public JiraIssueType getIssueType() {
        return getOrCreateField("project", JiraIssueType::new);
    }

    @JsonIgnore
    public void setIssueType(JiraIssueType issueType) {
        this.fields.put("issuetype", issueType);
    }

    @JsonIgnore
    public String getDescription() {
        return (String)this.fields.getOrDefault("description", "");
    }

    @JsonIgnore
    public void setDescription(String description) {
        this.fields.put("description", description);
    }
}
