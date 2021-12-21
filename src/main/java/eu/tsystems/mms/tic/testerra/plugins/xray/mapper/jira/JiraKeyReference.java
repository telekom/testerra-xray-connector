package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class JiraKeyReference extends JiraIdReference {
    private String key;

    public JiraKeyReference(Map<String, Object> map) {
        super(map);
        this.key = (String)map.getOrDefault("key", null);
    }

    public JiraKeyReference() {
    }

    public JiraKeyReference(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public boolean hasKey() {
        return StringUtils.isNotBlank(this.key);
    }
}
