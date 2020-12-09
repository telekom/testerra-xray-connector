package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.predef;

import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.FieldUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraArray;

public class TestPlan extends FieldUpdate {

    public TestPlan(String... ticketIds) {
        super(Fields.TEST_PLAN, new JiraArray(ticketIds));
    }
}
