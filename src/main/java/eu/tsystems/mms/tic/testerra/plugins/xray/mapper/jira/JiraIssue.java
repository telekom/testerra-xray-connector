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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;


public class JiraIssue extends JiraIssueKeyReference implements Loggable {
    private Map<String, Object> fields;

    public JiraIssue() {
        this.fields = new HashMap<>();
    }

    public JiraIssue(Map<String, Object> map) {
        super(map);
        this.fields = (Map<String, Object>)map.getOrDefault("fields", new HashMap<>());
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

    private <ENTITY extends Object> ENTITY getOrCreateEntity(String name, Function<Map<String,Object>, ENTITY> fieldSupplier) {
        ENTITY val;
        if (!this.fields.containsKey(name)) {
            val = fieldSupplier.apply(new HashMap<>());
            this.fields.put(name, val);
            return val;
        }

        Object fieldVal = this.fields.get(name);
        if (fieldVal instanceof Map) {
            val = fieldSupplier.apply((Map<String, Object>)fieldVal);
            this.fields.put(name, val);
        } else {
            val = (ENTITY)this.fields.get(name);
        }
        return val;
    }

    private <TYPE extends Object> TYPE getOrCreateField(String name, Supplier<TYPE> fieldSupplier) {
        TYPE val;
        if (!this.fields.containsKey(name)) {
            val = fieldSupplier.get();
            this.fields.put(name, val);
            return val;
        } else {
            return (TYPE)this.fields.get(name);
        }
    }

    @JsonIgnore
    public List<String> getLabels() {
        return getOrCreateField("labels", ArrayList::new);
    }

    @JsonIgnore
    public void setLabels(List<String> labels) {
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
        return getOrCreateEntity("status", JiraStatus::new);
    }

    @JsonIgnore
    public void setStatus(final JiraStatus status) {
        this.fields.put("status", status);
    }

    @JsonIgnore
    public JiraIssueKeyReference getProject() {
        return getOrCreateEntity("project", JiraIssueKeyReference::new);
    }

    @JsonIgnore
    public void setProject(JiraIssueKeyReference project) {
        this.fields.put("project", project);
    }

    @JsonIgnore
    public JiraIssueType getIssueType() {
        return getOrCreateEntity("issuetype", JiraIssueType::new);
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
