/*
 * Testerra Xray-Connector
 *
 * (C) 2021, Mike Reiche,  T-Systems MMS GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.TestType;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraKeyReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

/**
 * The Xray test execution import format differs from Standard Jira
 * @see <a href="https://docs.getxray.app/display/XRAY/Import+Execution+Results">Import+Execution+Results</a>
 */
public class XrayTestExecutionImport {

    /**
     * This date pattern differs from {@link JiraIssue#PATTERN_DATE_FORMAT}
     */
    private static final String PATTERN_DATE_FORMAT ="yyyy-MM-dd'T'HH:mm:ssXXX";

    public static abstract class AbstractInfo {
        private String summary;
        private String description;

        public String getSummary() {
            return summary;
        }

        public String getDescription() {
            return description;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Result {
        private JiraKeyReference testExecIssue;

        public JiraKeyReference getTestExecIssue() {
            return testExecIssue;
        }
    }

    public static class Info extends AbstractInfo {
        private String project;
        private String version;
        private String revision;
        private String user;
        private Date startDate;
        private Date finishDate;
        private String testPlanKey;
        private List<String> testEnvironments = new ArrayList<>();
        private List<String> fixVersions = null;

        public List<String> getFixVersions() {
            return fixVersions;
        }

        public void setFixVersions( List<String> fixVersions ) {
            this.fixVersions = fixVersions;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getRevision() {
            return revision;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN_DATE_FORMAT, timezone = "CET")
        public Date getStartDate() {
            return startDate;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN_DATE_FORMAT, timezone = "CET")
        public Date getFinishDate() {
            return finishDate;
        }

        public String getTestPlanKey() {
            return testPlanKey;
        }

        public List<String> getTestEnvironments() {
            return testEnvironments;
        }

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public void setFinishDate(Date finishDate) {
            this.finishDate = finishDate;
        }
    }

    public static class TestRun {
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

            private String projectKey;
            private List<String> labels;
            private List<XrayTestExecutionImport.TestStep> steps;
            private String testType;
            private String definition;

            public String getTestType() {
                return testType;
            }

            public void setType(TestType testType) {
                this.setTestType(testType.toString());
            }

            public void setTestType(String testType) {
                this.testType = testType;
            }

            public void addStep(XrayTestExecutionImport.TestStep step) {
                if (this.steps == null) {
                    this.steps = new ArrayList<>();
                }
                this.steps.add(step);
            }

            public void setSteps(List<XrayTestExecutionImport.TestStep> steps) {
                this.steps = steps;
            }

            public List<XrayTestExecutionImport.TestStep> getSteps() {
                return steps;
            }

            public String getProjectKey() {
                return projectKey;
            }

            public void setProjectKey(String projectKey) {
                this.projectKey = projectKey;
            }

            public List<String> getLabels() {
                return labels;
            }

            public void setLabels(List<String> labels) {
                this.labels = labels;
            }

            public String getDefinition() {
                return definition;
            }

            public void setDefinition(String definition) {
                this.definition = definition;
            }
        }

        public static class Evidence {
            private String data;
            private String filename;
            private String contentType;

            public Evidence() {

            }

            public Evidence(File file) throws IOException {
                this.filename = file.getName();
                byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
                this.data = new String(encoded, StandardCharsets.US_ASCII);
                MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
                this.contentType = fileTypeMap.getContentType(file.getName());
            }

            public String getContentType() {
                return contentType;
            }

            public void setMediaType(MediaType mediaType) {
                this.contentType = mediaType.toString();
            }

            public void setContentType(String contentType) {
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

        public static class Step {
            private Status status;
            private String actualResult;
            private Set<Evidence> evidences;

            public Step() {
            }

            public Status getStatus() {
                return status;
            }

            public void setStatus(Status status) {
                this.status = status;
            }

            public String getActualResult() {
                return actualResult;
            }

            public void setActualResult(String actualResult) {
                this.actualResult = actualResult;
            }

            public Set<Evidence> getEvidences() {
                return evidences;
            }

            public void setEvidences(Set<Evidence> evidence) {
                this.evidences = evidence;
            }

            public void addEvidence(Evidence evidence) {
                if (this.evidences == null) {
                    this.evidences = new HashSet<>();
                }
                this.evidences.add(evidence);
            }
        }

        private Info testInfo;
        private String testKey;
        private Date start;
        private Date finish;
        private String comment;
        private Set<Evidence> evidence;
        private Status status;
        private List<Step> steps;

        public TestRun() {
        }

        public TestRun(JiraIssue issue) {
            this(issue.getKey());
            this.testInfo = new Info();
            this.testInfo.setDescription(issue.getDescription());
            this.testInfo.setSummary(issue.getSummary());
            this.testInfo.setLabels(issue.getLabels());
            this.testInfo.setDefinition(issue.getSummary());
            this.testInfo.setType(TestType.AutomatedGeneric);
            this.testInfo.setProjectKey(issue.getProject().getKey());
        }

        public TestRun(String testKey) {
            this.testKey = testKey;
            this.setStatus(Status.FAIL);
        }

        public Info getTestInfo() {
            return testInfo;
        }

        public void setTestInfo(Info testInfo) {
            this.testInfo = testInfo;
        }

        public String getTestKey() {
            return testKey;
        }

        public void setTestKey(String testKey) {
            this.testKey = testKey;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN_DATE_FORMAT, timezone = "CET")
        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN_DATE_FORMAT, timezone = "CET")
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

        public Set<Evidence> getEvidence() {
            return evidence;
        }

        public void setEvidence(Set<Evidence> evidence) {
            this.evidence = evidence;
        }

        public void addEvidence(Evidence evidence) {
            if (this.evidence == null) {
                this.evidence = new HashSet<>();
            }
            this.evidence.add(evidence);
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public void addStep(Step step) {
            if (this.steps == null) {
                this.steps = new ArrayList<>();
            }
            this.steps.add(step);
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }

        public List<Step> getSteps() {
            return steps;
        }

    }

    private final Info info = new Info();
    private String testExecutionKey;
    private final Set<TestRun> testRuns = new HashSet<>();;

    public XrayTestExecutionImport(String testExecutionKey) {
        this.testExecutionKey = testExecutionKey;
    }

    public XrayTestExecutionImport(XrayTestExecutionIssue testExecutionIssue) {
        this.testExecutionKey = testExecutionIssue.getKey();
        this.info.setProject(testExecutionIssue.getProject().getKey());
        this.info.setDescription(testExecutionIssue.getDescription());
        this.info.setSummary(testExecutionIssue.getSummary());
        this.info.version = testExecutionIssue.getFixVersions().stream().findFirst().map(JiraNameReference::getName).orElse(null);
        this.info.revision = testExecutionIssue.getRevision();
        this.info.user = testExecutionIssue.getAssignee() != null ? testExecutionIssue.getAssignee().getName() : null;
        this.info.startDate = testExecutionIssue.getStartDate();
        this.info.finishDate = testExecutionIssue.getFinishDate();

        List<String> testPlanKeys = testExecutionIssue.getTestPlanKeys();
        if  (testPlanKeys != null && testPlanKeys.size() > 0) {
            this.info.testPlanKey = testPlanKeys.get(0);
        }

        List<String> testEnvironments = testExecutionIssue.getTestEnvironments();
        if (testEnvironments != null && testEnvironments.size() > 0) {
            this.info.testEnvironments = testEnvironments;
        }
    }

    public Info getInfo() {
        return info;
    }

    public void setTestExecutionKey(String testExecutionKey) {
        this.testExecutionKey = testExecutionKey;
    }

    public void setTests(Set<TestRun> testRuns) {
        this.testRuns.clear();
        this.addTests(testRuns);
    }

    public void addTests(Set<TestRun> testRuns) {
        this.testRuns.addAll(testRuns);
    }

    public void setTestKeys(Set<String> testKeys, TestRun.Status status) {
        this.testRuns.clear();
        this.addTestKeys(testKeys, status);
    }

    public void addTestKeys(Set<String> testKeys, TestRun.Status status) {
        this.testRuns.addAll(testKeys.stream()
                .map(TestRun::new)
                .peek(test -> test.setStatus(status))
                .collect(Collectors.toSet())
        );
    }

    public void addTest(TestRun testRun) {
        this.testRuns.add(testRun);
    }

    public Set<TestRun> getTests() {
        return this.testRuns;
    }

    public String getTestExecutionKey() {
        return testExecutionKey;
    }

    public static class TestStep {
        private String action;
        private String result;
        private String data;

        public TestStep() {

        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
