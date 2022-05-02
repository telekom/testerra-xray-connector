package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class XrayTestExecutionIssue extends XrayIssue {

    public XrayTestExecutionIssue() {
        this.setIssueType(IssueType.TestExecution.getIssueType());
        this.setStartDate(new Date());
        //this.setStatus(JiraStatus.NEW);
    }

    public XrayTestExecutionIssue(JiraIssue issue) {
        super(issue);
    }

    public XrayTestExecutionIssue(Map<String, Object> map) {
        super(map);
    }

    @JsonIgnore
    public List<String> getTestEnvironments() {
        return getOrCreateStringList(Fields.TEST_EXECUTION_TEST_ENVIRONMENTS.getFieldName());
    }

    @JsonIgnore
    public void setTestEnvironments(List<String> testEnvironments) {
        this.getFields().put(Fields.TEST_EXECUTION_TEST_ENVIRONMENTS.getFieldName(), testEnvironments);
    }

    @JsonIgnore
    public Date getStartDate() {
        return getDateFromField(Fields.TEST_EXECUTION_START_DATE.getFieldName()).orElse(null);
    }

    @JsonIgnore
    public void setStartDate(Date date) {
        this.getFields().put(Fields.TEST_EXECUTION_START_DATE.getFieldName(), this.dateToString(date));
    }

    @JsonIgnore
    public Date getFinishDate() {
        return getDateFromField(Fields.TEST_EXECUTION_FINISH_DATE.getFieldName()).orElse(null);
    }

    @JsonIgnore
    public void setFinishDate(Date date) {
        this.getFields().put(Fields.TEST_EXECUTION_FINISH_DATE.getFieldName(), this.dateToString(date));
    }

    @JsonIgnore
    public String getRevision() {
        return (String)this.getFields().getOrDefault(Fields.TEST_EXECUTION_REVISION.getFieldName(), null);
    }

    @JsonIgnore
    public void setRevision(String revision) {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!revision.matches(xrayConfig.getValidationRegexRevision())) {
            throw new RuntimeException(String.format("revision %s does not conform regex %s",
                    revision, xrayConfig.getValidationRegexRevision()));
        }
        this.getFields().put(Fields.TEST_EXECUTION_REVISION.getFieldName(), revision);
    }

    @JsonIgnore
    public List<String> getTestPlanKeys() {
        return getOrCreateStringList(Fields.TEST_EXECUTION_TEST_PLANS.getFieldName());
    }

    @JsonIgnore
    public void setTestPlanKeys(List<String> testPlanKeys) {
        this.getFields().put(Fields.TEST_EXECUTION_TEST_PLANS.getFieldName(), testPlanKeys);
    }
}
