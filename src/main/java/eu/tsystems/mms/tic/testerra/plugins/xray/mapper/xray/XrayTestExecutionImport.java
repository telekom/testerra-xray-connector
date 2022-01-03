package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.MediaTypeSerializer;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.MediaType;

public class XrayTestExecutionImport {

    public static abstract class AbstractInfo {
        private String project;
        private String summary;
        private String description;

        public String getProject() {
            return project;
        }

        public String getSummary() {
            return summary;
        }

        public String getDescription() {
            return description;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Info extends AbstractInfo {
        private String version;
        private String revision;
        private String user;
        private Date startDate;
        private Date finishDate;
        private String testPlanKey;
        private List<String> testEnvironments;

        public String getVersion() {
            return version;
        }

        public String getRevision() {
            return revision;
        }

        public String getUser() {
            return user;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = JiraIssue.PATTERN_DATE_FORMAT, timezone = "CET")
        public Date getStartDate() {
            return startDate;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = JiraIssue.PATTERN_DATE_FORMAT, timezone = "CET")
        public Date getFinishDate() {
            return finishDate;
        }

        public String getTestPlanKey() {
            return testPlanKey;
        }

        public List<String> getTestEnvironments() {
            return testEnvironments;
        }
    }

    public static class Test {
        public enum Status {
            PASS,
            PASSED_BEFORE,
            FAIL,
            TODO,
            EXECUTING,
            SKIPPED,
            ABORTED
        }
        public static class Info extends AbstractInfo {

        }

        public static class Evidence {
            private String data;
            private String filename;
            private MediaType contentType;

            @JsonSerialize(using = MediaTypeSerializer.class)
            public MediaType getContentType() {
                return contentType;
            }

            public void setContentType(MediaType contentType) {
                this.contentType = contentType;
            }

            public String getFilename() {
                return filename;
            }

            public void setFilename(String filename) {
                this.filename = filename;
            }

            public String getData() {
                return data;
            }

            public void setData(String data) {
                this.data = data;
            }
        }

        private final Info testInfo = new Info();
        private String testKey;
        private Date start;
        private Date finish;
        private String comment;
        private Set<Evidence> evidences;
        private Status status;

        public Info getTestInfo() {
            return testInfo;
        }

        public String getTestKey() {
            return testKey;
        }

        public void setTestKey(String testKey) {
            this.testKey = testKey;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = JiraIssue.PATTERN_DATE_FORMAT, timezone = "CET")
        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = JiraIssue.PATTERN_DATE_FORMAT, timezone = "CET")
        public Date getFinish() {
            return finish;
        }

        public void setFinish(Date finish) {
            this.finish = finish;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Set<Evidence> getEvidences() {
            return evidences;
        }

        public void setEvidences(Set<Evidence> evidences) {
            this.evidences = evidences;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }

    private final Info info = new Info();
    private final String testExecutionKey;
    private Set<Test> tests;

    public XrayTestExecutionImport() {
        this.testExecutionKey = null;
    }

    public XrayTestExecutionImport(String testExecutionKey) {
        this.testExecutionKey = testExecutionKey;
    }

    public XrayTestExecutionImport(XrayTestExecutionIssue testExecutionIssue) {
        this.testExecutionKey = testExecutionIssue.getKey();
        this.info.setProject(testExecutionIssue.getProject().getKey());
        this.info.setDescription(testExecutionIssue.getDescription());
        this.info.setSummary(testExecutionIssue.getSummary());
        this.info.version = testExecutionIssue.getVersions().stream().findFirst().map(JiraNameReference::getName).orElse(null);
        this.info.revision = testExecutionIssue.getRevision();
        this.info.user = testExecutionIssue.getAssignee().getName();
        this.info.startDate = testExecutionIssue.getStartDate();
        this.info.finishDate = testExecutionIssue.getFinishDate();
        /**
         * @todo Missing
         */
        this.info.testPlanKey = null;
        this.info.testEnvironments = testExecutionIssue.getTestEnvironments();
    }

    public Info getInfo() {
        return info;
    }

    public void setTests(Set<Test> tests) {
        this.tests = tests;
    }

    public Set<Test> getTests() {
        return this.tests;
    }

    public String getTestExecutionKey() {
        return testExecutionKey;
    }
}
