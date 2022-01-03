package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray;

import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraKeyReference;

public class XrayTestExecutionResult {
    private JiraKeyReference testExecIssue;

    public JiraKeyReference getTestExecIssue() {
        return testExecIssue;
    }

    public void setTestExecIssue(JiraKeyReference testExecIssue) {
        this.testExecIssue = testExecIssue;
    }
}
