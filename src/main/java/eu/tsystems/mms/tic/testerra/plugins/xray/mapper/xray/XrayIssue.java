package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import java.util.List;
import java.util.Map;

public class XrayIssue extends JiraIssue {
    public XrayIssue() {
    }
    public XrayIssue(JiraIssue issue) {
        super(issue);
    }

    public XrayIssue(Map<String, Object> map) {
        super(map);
    }

    @JsonIgnore
    public List<JiraNameReference> getFixVersions() {
        return getOrCreateEntityList(Fields.FIX_VERSIONS.getFieldName(), JiraNameReference::new);
    }

    @JsonIgnore
    public void setFixVersions(List<JiraNameReference> versions) {
        this.getFields().put(Fields.FIX_VERSIONS.getFieldName(), versions);
    }

    @JsonIgnore
    public String getRevision() {
        return (String)this.getFields().getOrDefault(Fields.REVISION.getFieldName(), null);
    }

    @JsonIgnore
    public void setRevision(String revision) {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!revision.matches(xrayConfig.getValidationRegexRevision())) {
            throw new RuntimeException(String.format("revision %s does not conform regex %s",
                    revision, xrayConfig.getValidationRegexRevision()));
        }
        this.getFields().put(Fields.REVISION.getFieldName(), revision);
    }

    @Override
    public void setSummary(String summary) {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!summary.matches(xrayConfig.getValidationRegexSummary())) {
            throw new RuntimeException(String.format("summary %s does not conform regex %s",
                    summary, xrayConfig.getValidationRegexSummary()));
        }
        super.setSummary(summary);
    }

    @Override
    public void setDescription(String description) {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!description.matches(xrayConfig.getValidationRegexDescription())) {
            throw new RuntimeException(String.format("description %s does not conform regex %s",
                    description, xrayConfig.getValidationRegexDescription()));
        }
        super.setDescription(description);
    }
}
