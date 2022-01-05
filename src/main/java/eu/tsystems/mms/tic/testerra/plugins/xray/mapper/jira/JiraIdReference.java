package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class JiraIdReference {
    /**
     * Is an Object, because some entities provide Integers, some Strings
     */
    private Object id;
    private String self;

    public JiraIdReference(Map<String, Object> map) {
        this(map.getOrDefault("id", null));
        this.self = (String)map.getOrDefault("self", null);
    }

    public JiraIdReference() {
    }

    public JiraIdReference(Object id) {
        this.id = id;
    }

    public String getId() {
        if (this.id != null) {
            return id.toString();
        } else {
            return null;
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasId() {
        return StringUtils.isNotBlank(this.id.toString());
    }

    public String getSelf() {
        return self;
    }
}
