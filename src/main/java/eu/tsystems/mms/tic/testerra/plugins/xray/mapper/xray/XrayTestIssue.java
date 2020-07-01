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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XrayTestIssue {

    private String testKey;
    private XrayTestStatus status;
    private Date start;
    private Date finish;
    private String comment;
    private Set<XrayEvidence> evidences;

    /**
     * for marshalling
     */
    public XrayTestIssue() {
    }

    public XrayTestIssue(final String testKey, final XrayTestStatus status, final Date start, final Date finish, final String comment) {
        this.testKey = testKey;
        this.status = status;
        this.start = start;
        this.finish = finish;
        this.comment = comment;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public Set<XrayEvidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(Set<XrayEvidence> evidences) {
        this.evidences = evidences;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(final String testKey) {
        this.testKey = testKey;
    }

    public XrayTestStatus getStatus() {
        return status;
    }

    public void setStatus(final XrayTestStatus status) {
        this.status = status;
    }

    public Date getStart() {
        return start;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "CET")
    public void setStart(final Date start) {
        this.start = start;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "CET")
    public Date getFinish() {
        return finish;
    }

    public void setFinish(final Date finish) {
        this.finish = finish;
    }
}
