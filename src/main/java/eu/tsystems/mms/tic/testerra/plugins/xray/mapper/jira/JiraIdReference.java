package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class JiraIdReference {
    private String id;
    private String self;

    public JiraIdReference(Map<String, Object> map) {
        this((String)map.getOrDefault("id", null));
        this.self = (String)map.getOrDefault("self", null);
    }

    public JiraIdReference() {
    }

    public JiraIdReference(String id) {
        this.id = id;
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
    public String getSelf() {
        return self;
    }
}
