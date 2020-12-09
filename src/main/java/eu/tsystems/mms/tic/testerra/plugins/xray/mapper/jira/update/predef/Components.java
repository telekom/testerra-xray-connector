package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.predef;

import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.FieldUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraNameValueArray;

public class Components extends FieldUpdate {

    public Components(String... names) {
        super(Fields.COMPONENTS, new JiraNameValueArray(names));
    }
}
