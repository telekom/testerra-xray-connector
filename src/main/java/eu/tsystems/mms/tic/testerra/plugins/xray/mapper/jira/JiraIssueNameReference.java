package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class JiraIssueNameReference extends JiraIssueKeyReference {
    private String name;

    public JiraIssueNameReference(Map<String, Object> map) {
        super(map);
        this.name = (String)map.getOrDefault("name", null);
    }

    public JiraIssueNameReference() {
        super();
    }

    public JiraIssueNameReference(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasName() {
        return StringUtils.isNotBlank(this.name);
    }

    public void setName(String name) {
        this.name = name;
    }
}
