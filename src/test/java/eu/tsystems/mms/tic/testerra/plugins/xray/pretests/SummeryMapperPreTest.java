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

package eu.tsystems.mms.tic.testerra.plugins.xray.pretests;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.hook.XrayConnectorHook;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.demotests.ClassAnnotatedTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.synchronizer.SummaryMapperResultsSynchronizer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

@XrayTestSet
public class SummeryMapperPreTest extends ClassAnnotatedTest {

    // TODO:
    // Cleanup test set with all tests --> needed update of REST https://docs.getxray.app/display/XRAY/Test+Executions+-+REST
    // Execute tests
    // Check if tests associate with test set

    @BeforeClass
    public void prepareWebResource() {
        XrayConfig.init("sync.test.properties");
        Assert.assertTrue(XrayConfig.getInstance().isSyncEnabled());
        XrayConnectorHook.getInstance().setXrayResultsSynchronizer(new SummaryMapperResultsSynchronizer(SummaryMapperResultsSynchronizer.EXECUTION_SUMMARY2));
    }

}
