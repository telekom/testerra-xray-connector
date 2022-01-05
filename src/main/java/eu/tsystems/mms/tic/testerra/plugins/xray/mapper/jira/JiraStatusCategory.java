package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;

public class JiraStatusCategory extends JiraNameReference {
    public static final JiraStatusCategory NEW = new JiraStatusCategory("new");
    public static final JiraStatusCategory INDETERMINATE = new JiraStatusCategory("indeterminate");
    public static final JiraStatusCategory DONE = new JiraStatusCategory("done");

    public JiraStatusCategory() {
    }

    public JiraStatusCategory(String key) {
        super();
        this.setKey(key);
    }

    public JiraStatusCategory(Map<String, Object> map) {
        super(map);
    }
}
