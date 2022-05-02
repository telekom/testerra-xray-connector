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

package eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined;

import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayMapper;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;

public enum IssueType {
    Test(new JiraIssueType(PropertyManager.getProperty(XrayMapper.PROPERTY_TEST_ISSUETYPE_NAME, "Test"))),
    TestExecution(new JiraIssueType(PropertyManager.getProperty(XrayMapper.PROPERTY_TEST_EXECUTION_ISSUETYPE_NAME, "Test Execution"))),
    TestSet(new JiraIssueType(PropertyManager.getProperty(XrayMapper.PROPERTY_TEST_SET_ISSUETYPE_NAME,"Test Set"))),
    ;

    private final JiraIssueType issueType;

    IssueType(JiraIssueType literal) {
        this.issueType = literal;
    }

    @Override
    public String toString() {
        return issueType.getName();
    }

    public JiraIssueType getIssueType() {
        return this.issueType;
    }
}
