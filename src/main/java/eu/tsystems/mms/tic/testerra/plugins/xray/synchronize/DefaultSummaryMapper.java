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

    // name of the method is probably not unique in the whole project,
    // prefix it with name of the test class
    protected String makeDefaultSummaryForMethod(MethodContext methodContext) {
        return String.format(
                "%s_%s",
                methodContext.getClassContext().getTestClass().getSimpleName(),
                methodContext.getName());
    }

    @Override
    public JqlQuery queryTest(MethodContext methodContext) {
        return JqlQuery.create()
                .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                .addCondition(new IssueTypeEquals(IssueType.Test))
                .addCondition(new SummaryContainsExact( makeDefaultSummaryForMethod( methodContext ) ) )
                .build();
    }

    @Override
    public boolean shouldCreateNewTest(MethodContext methodContext) {
        return true;
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
        return true;
    }

    @Override
    public void updateTest(XrayTestIssue xrayTestIssue, MethodContext methodContext) {
        xrayTestIssue.setSummary( makeDefaultSummaryForMethod( methodContext ) );
    }

    @Override
    public void updateTestSet(XrayTestSetIssue xrayTestSetIssue, ClassContext classContext) {
        xrayTestSetIssue.setSummary(classContext.getTestClassContext().map(TestClassContext::name).orElse(classContext.getName()));
    }
}
