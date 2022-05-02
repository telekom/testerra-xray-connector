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
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class JiraIssue extends JiraKeyReference implements Loggable {
    private final Map<String, Object> fields;
    public static final String PATTERN_DATE_FORMAT ="yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(PATTERN_DATE_FORMAT);

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
        this.setId(issue.getId());
        this.fields = issue.fields;
    }

    public Map<String, Object> getFields() {
        return this.fields;
    }

    protected Date dateFromString(String dateString) {
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            log().warn(String.format("Unable to parse date string: %s", dateString), e);
            return null;
        }
    }

    protected String dateToString(Date date) {
        return DATE_FORMAT.format(date);
    }

    protected Optional<Date> getDateFromField(String name) {
        String dateString = (String)this.getFields().getOrDefault(name, null);
        Date date = null;
        if (dateString != null) {
            date = this.dateFromString(dateString);
        }
        return Optional.ofNullable(date);
    }

    protected <ENTITY, TYPE> ENTITY getOrCreateField(
            String name,
            Class<TYPE> jiraType,
            Function<TYPE, ENTITY> fieldSupplier
    ) {
        ENTITY val;
        if (!this.fields.containsKey(name)) {
            val = fieldSupplier.apply(null);
            this.fields.put(name, val);
            return val;
        }

        Object jiraField = this.fields.get(name);
        if (jiraType.isInstance(jiraField)) {
            val = fieldSupplier.apply((TYPE)jiraField);
            this.fields.put(name, val);
        } else {
            val = (ENTITY)this.fields.get(name);
        }
        return val;
    }

    protected <ENTITY extends Object> ENTITY getOrCreateEntity(String name, Function<Map<String, Object>, ENTITY> entitySupplier) {
        return getOrCreateField(name, Map.class, map -> {
            if (map == null) {
                map = new HashMap<String, Object>();
            }
            return entitySupplier.apply((Map<String, Object>)map);
        });
    }

    protected <ENTITY> List<ENTITY> getOrCreateFieldList(String name, Function<List<? extends Object>, List<ENTITY>> listSupplier) {
        return this.getOrCreateField(name, List.class, list -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            return listSupplier.apply(list);
        });
    }

    protected <ENTITY> List<ENTITY> getOrCreateEntityList(String name, Function<Map<String, Object>, ENTITY> entitySupplier) {
        return this.getOrCreateFieldList(name, list -> {
            return list.stream()
                    .map(o -> {
                        if (o instanceof Map) {
                            // If the existing element is a map, convert it to entity
                            return entitySupplier.apply((Map<String, Object>)o);
                        } else {
                            // Otherwise, return entity only
                            return (ENTITY)o;
                        }
                    })
                    .collect(Collectors.toList());
        });
    }

    protected List<String> getOrCreateStringList(String name) {
        return getOrCreateFieldList(name, list -> (List<String>)list);
    }

    @JsonIgnore
    public List<String> getLabels() {
        return getOrCreateStringList(Fields.LABELS.getFieldName());
    }

    @JsonIgnore
    public void setLabels(List<String> labels) {
        this.fields.put(Fields.LABELS.getFieldName(), labels);
    }

    @JsonIgnore
    public String getSummary() {
        return (String)this.fields.getOrDefault(Fields.SUMMARY.getFieldName(), null);
    }

    @JsonIgnore
    public void setSummary(final String summary) {
        this.fields.put(Fields.SUMMARY.getFieldName(), summary);
    }

    @JsonIgnore
    public JiraStatus getStatus() {
        return getOrCreateEntity(Fields.STATUS.getFieldName(), JiraStatus::new);
    }

    @JsonIgnore
    public boolean hasStatus(JiraStatus status) {
        JiraStatus myStatus = getStatus();
        return (myStatus.hasName() && status.hasName() && myStatus.getName().equals(status.getName()));
    }

    @JsonIgnore
    public void setStatus(final JiraStatus status) {
        this.fields.put(Fields.STATUS.getFieldName(), status);
    }

    @JsonIgnore
    public JiraNameReference getProject() {
        return getOrCreateEntity(Fields.PROJECT.getFieldName(), JiraNameReference::new);
    }

    @JsonIgnore
    public void setProject(JiraNameReference project) {
        this.fields.put(Fields.PROJECT.getFieldName(), project);
    }

    @JsonIgnore
    public JiraIssueType getIssueType() {
        return getOrCreateEntity(Fields.ISSUE_TYPE.getFieldName(), JiraIssueType::new);
    }

    @JsonIgnore
    public void setIssueType(JiraIssueType issueType) {
        this.fields.put(Fields.ISSUE_TYPE.getFieldName(), issueType);
    }

    @JsonIgnore
    public String getDescription() {
        return (String)this.fields.getOrDefault(Fields.DESCRIPTION.getFieldName(), null);
    }

    @JsonIgnore
    public void setDescription(String description) {
        this.fields.put(Fields.DESCRIPTION.getFieldName(), description);
    }

    @JsonIgnore
    public JiraNameReference getAssignee() {
        return getOrCreateEntity(Fields.ASSIGNEE.getFieldName(), JiraNameReference::new);
    }

    @JsonIgnore
    public void setAssignee(JiraNameReference assignee) {
        this.fields.put(Fields.ASSIGNEE.getFieldName(), assignee);
    }

    @JsonIgnore
    public List<JiraAttachment> getAttachments() {
        return getOrCreateEntityList(Fields.ATTACHMENT.getFieldName(), JiraAttachment::new);
    }

    @JsonIgnore
    public void setAttachments(List<JiraAttachment> attachments) {
        this.fields.put(Fields.ATTACHMENT.getFieldName(), attachments);
    }

    @JsonIgnore
    public List<JiraNameReference> getVersions() {
        return getOrCreateEntityList(Fields.VERSIONS.getFieldName(), JiraNameReference::new);
    }

    @JsonIgnore
    public void setVersions(List<JiraNameReference> versions) {
        this.fields.put(Fields.VERSIONS.getFieldName(), versions);
    }
}
