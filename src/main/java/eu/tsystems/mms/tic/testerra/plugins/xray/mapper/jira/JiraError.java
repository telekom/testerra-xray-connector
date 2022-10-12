package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 2022-10-11
 *
 * @author mgn
 */
public class JiraError {

    private String[] messages;

    private String summary;

    private String project;

    public List<String> getMessages() {
        return Arrays.asList(messages);
    }

    public String getSummary() {
        return summary;
    }

    public String getProject() {
        return project;
    }
}
