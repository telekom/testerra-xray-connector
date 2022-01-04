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

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import java.util.List;
import java.util.Map;

public class XrayTestSetIssue extends XrayIssue {

    public XrayTestSetIssue() {
        this.setIssueType(IssueType.TestSet.getIssueType());
    }

    public XrayTestSetIssue(JiraIssue issue) {
        super(issue);
    }

    public XrayTestSetIssue(Map<String, Object> map) {
        super(map);
    }

    @JsonIgnore
    public List<String> getTestKeys() {
        return getOrCreateStringList(Fields.TEST_SET_TESTS.getFieldName());
    }

    public void setTestKeys(List<String> testKeys) {
        this.getFields().put(Fields.TEST_SET_TESTS.getFieldName(), testKeys);
    }

}
