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

package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.info;

import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayTestExecutionInfo;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTestExecutionInfo implements XrayTestExecutionInfo {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getSummary() {
        logger.debug("getting summary");
        return eu.tsystems.mms.tic.testerra.plugins.xray.TestData.TEST_EXEC_SUMMARY_DEFAULT;
    }

    @Override
    public String getDescription() {
        logger.debug("getting description");
        return "simulated test run";
    }

    @Override
    public String getRevision() {
        logger.debug("getting revision");
        return eu.tsystems.mms.tic.testerra.plugins.xray.TestData.TEST_EXEC_REVISION_DEFAULT;
    }

    @Override
    public String getAssignee() {
        return null;
    }

    @Override
    public String getFixVersion() {
        return null;
    }

    @Override
    public List<String> getTestEnvironments() {
        return eu.tsystems.mms.tic.testerra.plugins.xray.TestData.TEST_EXEC_TEST_ENVIRONMENTS_DEFAULT;
    }
}
