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

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class XrayInfo implements Serializable {

    private String project;
    private String summary;
    private String description;
    private String version;
    private String user;
    private String revision;
    private List<String> testEnvironments;
    private Date startDate;
    private Date finishDate;

    public XrayInfo() {
    }

    public XrayInfo(String project, String summary, String description, String revision) {
        this.project = project;
        this.summary = summary;
        this.description = description;
        this.revision = revision;
    }

    public XrayInfo(
            String project,
            String summary,
            String description,
            List<String> testEnvironments,
            String revision,
            String fixVersion,
            String assignee
    ) {
        this.project = project;
        this.summary = summary;
        this.description = description;
        this.revision = revision;
        setTestEnvironments(testEnvironments);
        version = fixVersion;
        user = assignee;
    }

    /**
     * @deprecated Use {@link #readTestEnvironments()} instead
     * @return
     */
    public List<String> getTestEnvironments() {
        return testEnvironments;
    }

    public void setTestEnvironments(List<String> testEnvironments) {
        if (testEnvironments != null) {
            this.testEnvironments = Collections.unmodifiableList(testEnvironments);
        }
    }

    public Stream<String> readTestEnvironments() {
        if (this.testEnvironments==null) {
            return Stream.empty();
        } else {
            return this.testEnvironments.stream();
        }
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(final String revision) {
        this.revision = revision;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "CET")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "CET")
    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(final Date finishDate) {
        this.finishDate = finishDate;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
