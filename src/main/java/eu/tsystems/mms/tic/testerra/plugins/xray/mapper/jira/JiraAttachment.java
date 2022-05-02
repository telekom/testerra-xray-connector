package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;

public class JiraAttachment extends JiraIdReference {
    private String filename;

    public JiraAttachment(Map<String, Object> map) {
        super(map);
        this.filename = (String)map.getOrDefault("filename", null);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
