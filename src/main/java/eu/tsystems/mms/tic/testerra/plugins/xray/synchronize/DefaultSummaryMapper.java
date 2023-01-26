package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueTypeEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.ProjectEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.SummaryContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testframework.annotations.TestClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;

public class DefaultSummaryMapper implements XrayMapper {

    @Override
    public JqlQuery queryTest(MethodContext methodContext) {
        return JqlQuery.create()
                .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                .addCondition(new IssueTypeEquals(IssueType.Test))
                .addCondition(new SummaryContainsExact(this.getDefaultTestIssueSummery(methodContext)))
                .build();
    }

    @Override
    public boolean shouldCreateNewTest(MethodContext methodContext) {
        return false;
    }

    @Override
    public JqlQuery queryTestSet(ClassContext classContext) {
        return JqlQuery.create()
                .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                .addCondition(new IssueTypeEquals(IssueType.TestSet))
                .addCondition(new SummaryContainsExact(classContext.getTestClassContext().map(TestClassContext::name).orElse(classContext.getName())))
                .build();
    }

    @Override
    public boolean shouldCreateNewTestSet(ClassContext classContext) {
        return false;
    }

    @Override
    public void updateTest(XrayTestIssue xrayTestIssue, MethodContext methodContext) {
        xrayTestIssue.setSummary(this.getDefaultTestIssueSummery(methodContext));
    }

    @Override
    public void updateTestSet(XrayTestSetIssue xrayTestSetIssue, ClassContext classContext) {
        xrayTestSetIssue.setSummary(classContext.getTestClassContext().map(TestClassContext::name).orElse(classContext.getName()));
    }
}
