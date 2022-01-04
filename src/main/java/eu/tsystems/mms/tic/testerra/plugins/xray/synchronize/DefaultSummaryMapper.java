package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.ProjectEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.SummaryContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testframework.annotations.TestClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import java.util.Optional;
import org.testng.ITestClass;
import org.testng.ITestResult;

public class DefaultSummaryMapper implements XrayMapper {

    @Override
    public Optional<JqlQuery> createXrayTestQuery(MethodContext methodContext) {
        return Optional.of(
                JqlQuery.create()
                        .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                        .addCondition(new SummaryContainsExact(methodContext.getName()))
                        .build()
        );
    }

    @Override
    public Optional<JqlQuery> createXrayTestSetQuery(ClassContext classContext) {
        return Optional.of(
                JqlQuery.create()
                        .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                        .addCondition(new SummaryContainsExact(classContext.getTestClassContext().map(TestClassContext::name).orElse(classContext.getName())))
                        .build()
        );
    }

    @Override
    public void updateXrayTest(JiraIssue xrayTestIssue, MethodContext methodContext) {
        xrayTestIssue.setSummary(methodContext.getName());
    }

    @Override
    public void updateXrayTestSet(XrayTestSetIssue xrayTestSetIssue, ClassContext classContext) {
        xrayTestSetIssue.setSummary(classContext.getTestClassContext().map(TestClassContext::name).orElse(classContext.getName()));
    }
}
