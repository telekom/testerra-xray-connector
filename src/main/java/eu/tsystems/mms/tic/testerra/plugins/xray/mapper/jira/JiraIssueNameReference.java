package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import org.apache.commons.lang3.StringUtils;

public class JiraIssueNameReference extends JiraIssueKeyReference {
    private String name;

    public JiraIssueNameReference() {
    }

    public JiraIssueNameReference(String name) {
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
