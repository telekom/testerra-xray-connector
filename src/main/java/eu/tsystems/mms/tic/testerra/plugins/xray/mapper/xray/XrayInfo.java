/*
 * Testerra Xray-Connector
 *
 * (C) 2020, Martin Cölln, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
 *
 */

package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray;

import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @deprecated Use {@link XrayTestExecutionIssue}
 */
public class XrayInfo extends XrayTestExecutionIssue {
    public XrayInfo() {
    }

    @Deprecated
    public XrayInfo(String project, String summary, String description, String revision) {
        this.setProject(project);
        this.setSummary(summary);
        this.setDescription(description);
        this.setRevision(revision);
        this.setStartDate(new Date());
    }

    @Deprecated
    public XrayInfo(
            String project,
            String summary,
            String description,
            List<String> testEnvironments,
            String revision,
            String fixVersion,
            String assignee
    ) {
        this(project, summary, description, revision);
        setLabels(testEnvironments);
        setVersion(fixVersion);
        setUser(assignee);
    }

    /**
     * @deprecated Use {@link #getFixVersions()} instead
     */
    public String getVersion() {
        return getFixVersions().stream().findFirst().map(JiraNameReference::getName).orElse(null);
    }

    /**
     * @deprecated Use {@link #setFixVersions(List)} instead
     */
    public void setVersion(final String version) {
        ArrayList<JiraNameReference> jiraIssueNameReferences = new ArrayList<>();
        jiraIssueNameReferences.add(new JiraNameReference(version));
        this.setFixVersions(jiraIssueNameReferences);
    }

    /**
     * @deprecated Use {@link #getAssignee()} instead
     */
    public String getUser() {
        return getAssignee().getName();
    }

    /**
     * @deprecated Use {@link #setAssignee(JiraNameReference)} instead
     */
    public void setUser(final String user) {
        this.setAssignee(new JiraNameReference(user));
    }

    /**
     * @deprecated Use {@link #getProject()} instead
     */
    public String getProjectKey() {
        return super.getProject().getKey();
    }

    /**
     * @deprecated Use {@link #setProject(JiraNameReference)} instead
     */
    public void setProject(String project) {
        getProject().setKey(project);
    }
}
