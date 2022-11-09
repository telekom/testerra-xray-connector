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

import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.TestUtils;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.hook.XrayConnectorHook;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.demotests.ClassAnnotatedTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.synchronizer.EmptyMapperResultsSynchronizer;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import java.io.IOException;

@XrayTestSet(key = "SWFTE-136")
public class EmptyMapper2PreTest extends ClassAnnotatedTest {

    // TODO:
    // Cleanup test set with all tests --> needed update of REST https://docs.getxray.app/display/XRAY/Test+Executions+-+REST
    // Execute tests
    // Check if tests associate with test set

    @BeforeClass
    public void prepareWebResource() {
        XrayConfig.init("sync.test.properties");
        Assert.assertTrue(XrayConfig.getInstance().isSyncEnabled());
        XrayConnectorHook.getInstance().setXrayResultsSynchronizer(new EmptyMapperResultsSynchronizer());
    }

//    private static String createTestSet() {
//        try {
//            WebResource webResource = TestUtils.prepareWebResource("xray.properties");
//            final XrayConfig xrayConfig = XrayConfig.getInstance();
//            XrayUtils xrayUtils = new XrayUtils(webResource);
//            XrayTestSetIssue testSet = new XrayTestSetIssue();
//            testSet.setSummary("Testerra Xray Connector TestSet");
//            testSet.getProject().setKey(xrayConfig.getProjectKey());
//            testSet.setDescription("Testerra Xray Connector TestSet for ClassAnnotated test");
//
//            xrayUtils.createOrUpdateIssue(testSet);
//            return testSet.getKey();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
////        XrayTestSetIssue updatedIssue = xrayUtils.getIssue(testSet.getKey(), XrayTestSetIssue::new);
//
//    }

}
