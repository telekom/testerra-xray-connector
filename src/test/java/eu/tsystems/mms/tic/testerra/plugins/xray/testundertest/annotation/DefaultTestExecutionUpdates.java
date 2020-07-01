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

package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation;

import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.JiraIssueUpdate;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.predef.SetAffectedVersions;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.predef.SetLabels;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayTestExecutionUpdates;

public class DefaultTestExecutionUpdates implements XrayTestExecutionUpdates {

    @Override
    public JiraIssueUpdate updateOnNewExecutionCreated() {
        return JiraIssueUpdate.create()
                .field(new SetLabels("Test-Automatisierung"))
                .build();
    }

    @Override
    public JiraIssueUpdate updateOnExistingExecutionUpdated() {
        return JiraIssueUpdate.create()
                .field(new SetLabels("Test-Automatisierung"))
                .build();
    }

    @Override
    public JiraIssueUpdate updateOnExecutionDone() {
        return JiraIssueUpdate.create()
                .field(new SetAffectedVersions("1.0.2", "1.0.3"))
                .build();
    }
}
