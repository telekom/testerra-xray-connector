package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class JiraIssueKeyReference {
    private String id;
    private String key;

    public JiraIssueKeyReference(Map<String, Object> map) {
        this.id = (String)map.getOrDefault("id", null);
        this.key = (String)map.getOrDefault("key", null);
    }

    public JiraIssueKeyReference() {

    }

    public JiraIssueKeyReference(String key) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasId() {
        return StringUtils.isNotBlank(this.id);
    }
}
