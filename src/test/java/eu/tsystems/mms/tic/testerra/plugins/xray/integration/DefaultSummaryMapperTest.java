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

package eu.tsystems.mms.tic.testerra.plugins.xray.integration;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.hook.XrayConnectorHook;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.TestBase;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.synchronizer.SummaryMapperResultsSynchronizer;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStep;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@XrayTestSet
public class DefaultSummaryMapperTest extends TestBase {

    @BeforeClass
    public void prepareWebResource() {
        XrayConfig.init("sync.test.properties");
        Assert.assertTrue(XrayConfig.getInstance().isSyncEnabled());
        XrayConnectorHook.getInstance().setXrayResultsSynchronizer(new SummaryMapperResultsSynchronizer());
    }

    @Override
    @Test
    @XrayTest()
    public void passes() {
        super.passes();
    }

    @Override
    @Test
    @XrayTest()
    public void fails() {
        super.fails();
    }

    @Override
    @Test(dependsOnMethods = "fails")
    @XrayTest()
    public void skips() {
        super.skips();
    }

    @Test
    @XrayTest
    public void testWithSteps() {
        TestStep.begin("First step");
        TestStep.begin("Second step");
    }
}