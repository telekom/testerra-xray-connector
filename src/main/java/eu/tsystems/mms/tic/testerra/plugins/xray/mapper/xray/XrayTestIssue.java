package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray;

import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import java.util.Map;

public class XrayTestIssue extends XrayIssue {
    public XrayTestIssue() {
        this.setIssueType(IssueType.Test.getIssueType());
    }

    public XrayTestIssue(String issueKey) {
        this();
        this.setKey(issueKey);
    }

    public XrayTestIssue(JiraIssue issue) {
        super(issue);
    }

    public XrayTestIssue(Map<String, Object> map) {
        super(map);
    }
}
