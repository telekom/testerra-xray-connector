package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        this.getFields().put(Fields.REVISION.getFieldName(), revision);
    }

}
