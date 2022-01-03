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

    static class SimpleJiraIssueUpdateBuilder {

        private final ObjectMapper om;
        private final ObjectNode fields;

        SimpleJiraIssueUpdateBuilder() {
            om = new ObjectMapper();
            fields = om.createObjectNode();
        }

        public SimpleJiraIssueUpdateBuilder setLabels(final Collection<String> labels) {
            final ArrayNode labelsNode = om.createArrayNode();
            for (final String label : labels) {
                labelsNode.add(label);
            }
            fields.put("labels", labelsNode);
            return this;
        }

        public SimpleJiraIssueUpdateBuilder setFixVersions(final Collection<String> versionNames) {
            final ArrayNode versionsNode = getVersionNode(versionNames);
            fields.put("fixVersions", versionsNode);
            return this;
        }

        public SimpleJiraIssueUpdateBuilder setAffectedVersions(final Collection<String> versionNames) {
            final ArrayNode versionNamesNode = getVersionNode(versionNames);
            fields.put("versions", versionNamesNode);
            return this;
        }

        private ArrayNode getVersionNode(final Collection<String> versionNames) {
            final ArrayNode versionNamesNode = om.createArrayNode();
            for (final String versionName : versionNames) {
                versionNamesNode.add(om.createObjectNode().put("name", versionName));
            }
            return versionNamesNode;
        }


        public JiraIssueUpdate build() {
            final SimpleJiraIssueUpdate simpleJiraIssueUpdate = new SimpleJiraIssueUpdate();
            simpleJiraIssueUpdate.setFields(fields);
            return simpleJiraIssueUpdate;
        }
    }

    public static class JiraIssueUpdateBuilder {

        private final ObjectMapper om;
        private final ObjectNode updateNode;
        private final Map<String, Set<Combi>> fieldOperations;

        JiraIssueUpdateBuilder() {
            om = new ObjectMapper();
            updateNode = om.createObjectNode();
            fieldOperations = new HashMap<>();
        }

        public JiraIssueUpdateBuilder field(final String fieldName, final JiraVerb jiraVerb, final JiraValue jiraValue) {
            if (fieldOperations.containsKey(fieldName)) {
                fieldOperations.get(fieldName).add(new Combi(jiraVerb, jiraValue));
            } else {
                fieldOperations.put(fieldName, Sets.newHashSet(new Combi(jiraVerb, jiraValue)));
            }
            return this;
        }

        public JiraIssueUpdateBuilder field(FieldUpdate fieldUpdate) {
            return field(fieldUpdate.getFieldName(), fieldUpdate.getJiraVerb(), fieldUpdate.getJiraValue());
        }

        public JiraIssueUpdate build() {

            //            final ObjectNode objectNode = om.createObjectNode();
            for (final String fieldName : fieldOperations.keySet()) {

                final ArrayNode arrayNode = om.createArrayNode();
                for (final Combi combi : fieldOperations.get(fieldName)) {
                    final ObjectNode innerNode = om.createObjectNode();
                    //                    innerNode.put(combi.jiraVerb.name().toLowerCase(), combi.jiraValue.getValue());
                    innerNode.putPOJO(combi.jiraVerb.name().toLowerCase(), combi.jiraValue.getValue());
                    arrayNode.add(innerNode);
                }
                updateNode.put(fieldName, arrayNode);
            }

            final ExplicitJiraIssueUpdate explicitJiraIssueUpdate = new ExplicitJiraIssueUpdate();
            explicitJiraIssueUpdate.setUpdate(updateNode);
            return explicitJiraIssueUpdate;
        }

        class Combi {

            private final JiraVerb jiraVerb;
            private final JiraValue jiraValue;

            Combi(final JiraVerb jiraVerb, final JiraValue jiraValue) {
                this.jiraVerb = jiraVerb;
                this.jiraValue = jiraValue;
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Combi)) {
                    return false;
                }

                final Combi combi = (Combi) o;

                return jiraVerb == combi.jiraVerb && jiraValue.equals(combi.jiraValue);

            }

            @Override
            public int hashCode() {
                int result = jiraVerb.hashCode();
                result = 31 * result + jiraValue.hashCode();
                return result;
            }
        }

    }
}
