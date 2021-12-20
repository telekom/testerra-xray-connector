package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import java.util.Optional;
import org.testng.ITestClass;
import org.testng.ITestResult;

public class DefaultSummaryMapper implements XrayMapper {
    @Override
    public Optional<JqlQuery> createXrayTestQuery(ITestResult testNgResult) {
        return Optional.empty();
    }

    @Override
    public Optional<JqlQuery> createXrayTestSetQuery(ITestClass testNgClass) {
        return Optional.empty();
    }
}
