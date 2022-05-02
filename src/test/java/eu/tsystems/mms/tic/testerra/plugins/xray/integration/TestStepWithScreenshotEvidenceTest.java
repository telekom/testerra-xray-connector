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

package eu.tsystems.mms.tic.testerra.plugins.xray.integration;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.hook.XrayConnectorHook;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation.AbstractAnnotationsWithoutKeys;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.synchronizer.SummaryMapperResultsSynchronizer;
import eu.tsystems.mms.tic.testframework.annotations.Fails;
import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStep;
import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.desktop.WebDriverMode;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@XrayTestSet
public class TestStepWithScreenshotEvidenceTest extends TesterraTest {

    @BeforeClass
    public void prepareWebResource() {
        XrayConfig.init("sync.test.properties");
        Assert.assertTrue(XrayConfig.getInstance().isSyncEnabled());
        XrayConnectorHook.getInstance().setXrayResultsSynchronizer(new SummaryMapperResultsSynchronizer());
    }

    @Test
    @Fails
    @XrayTest
    public void test_testStepsWithScreenshotEvidence_fails() {
        TestStep.begin("Setup browser");

        DesktopWebDriverRequest desktopWebDriverRequest = new DesktopWebDriverRequest();
        desktopWebDriverRequest.setBrowser(Browsers.chromeHeadless);
        desktopWebDriverRequest.setWebDriverMode(WebDriverMode.local);

        WebDriver webDriver = WebDriverManager.getWebDriver(desktopWebDriverRequest);

        TestStep.begin("Browse google");
        webDriver.get("https://www.heise.de");

        TestStep.begin("Check if on google");
        Assert.assertEquals(webDriver.getTitle(), "Google");
    }
}
