package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;

public class JiraIssueType extends JiraNameReference {
    public JiraIssueType(Map<String, Object> map) {
        super(map);
    }
    public JiraIssueType() {
        super();
    }
    public JiraIssueType(String name) {
        super(name);
    }
}
