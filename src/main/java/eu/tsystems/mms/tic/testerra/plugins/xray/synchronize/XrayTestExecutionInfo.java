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

package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import java.util.List;


public interface XrayTestExecutionInfo {

    /**
     * @return summary to be used for Xray test execution issue
     */
    String getSummary();

    /**
     * @return description to be used for Xray test execution issue
     */
    String getDescription();

    /**
     * @return revision to be used for Xray test execution issue
     */
    String getRevision();

    /**
     * @return assignee to be used for Xray test execution issue or null
     */
    String getAssignee();

    /**
     * @return fixVersion to be used for Xray test execution issue or null
     */
    String getFixVersion();

    /**
     * @return test-environments to be used for Xray test execution issue or null
     */
    List<String> getTestEnvironments();

}
