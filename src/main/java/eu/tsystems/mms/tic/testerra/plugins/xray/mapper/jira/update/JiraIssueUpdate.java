package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by maco on 01.12.2015.
 */
@Deprecated
public abstract class JiraIssueUpdate {

    public static SimpleJiraIssueUpdateBuilder createSimple() {
        return new SimpleJiraIssueUpdateBuilder();
    }

    public static JiraIssueUpdateBuilder create() {
        return new JiraIssueUpdateBuilder();
    }

    @Deprecated
    static class SimpleJiraIssueUpdateBuilder {

        SimpleJiraIssueUpdateBuilder() {
        }

        public SimpleJiraIssueUpdateBuilder setLabels(final Collection<String> labels) {
            return this;
        }

        public SimpleJiraIssueUpdateBuilder setFixVersions(final Collection<String> versionNames) {
            return this;
        }

        public SimpleJiraIssueUpdateBuilder setAffectedVersions(final Collection<String> versionNames) {
            return this;
        }

        public JiraIssueUpdate build() {
            return new SimpleJiraIssueUpdate();
        }
    }

    public static class JiraIssueUpdateBuilder extends SimpleJiraIssueUpdateBuilder {
        JiraIssueUpdateBuilder() {
        }

        public JiraIssueUpdateBuilder field(final String fieldName, final JiraVerb jiraVerb, final JiraValue jiraValue) {
            return this;
        }

        public JiraIssueUpdateBuilder field(FieldUpdate fieldUpdate) {
            return field(fieldUpdate.getFieldName(), fieldUpdate.getJiraVerb(), fieldUpdate.getJiraValue());
        }
    }
}
