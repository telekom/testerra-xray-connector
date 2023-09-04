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

package eu.tsystems.mms.tic.testerra.plugins.xray.pretests.demotests;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStep;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodsAnnotatedWithStepsTest extends AbstractTestBase {


    @Test()
    @XrayTest(key = "SWFTE-141")
    public void testMapperWithStepsPasses() {
        TestStep.begin("Step 1");
        TestStep.begin("Step 2");
    }

    @Test()
    @XrayTest(key = "SWFTE-145")
    public void testMapperWithStepsFailes() {
        WEB_DRIVER_MANAGER.getWebDriver();
        TestStep.begin("Step 1");
        TestStep.begin("Step 2");
        Assert.fail("Failed step");
    }

//
//    @Override
//    @Test(dependsOnMethods = "testMapperFails")
//    @XrayTest(key = "SWFTE-133")
//    public void testMapperSkips() {
//        super.testMapperSkips();
//    }

    public static Map<String, XrayTestExecutionImport.TestRun> getExpectedTestRuns() {

        Map<String, XrayTestExecutionImport.TestRun> runMap = new HashMap<>();

        // Test run 1 with passed steps
        XrayTestExecutionImport.TestRun run1 = new XrayTestExecutionImport.TestRun("SWFTE-141");
        run1.setStatus(XrayTestExecutionImport.TestRun.Status.PASS);
        run1.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
        run1.getTestInfo().setSummary("testMapperTestStepsPassed");

        List<XrayTestExecutionImport.TestRun.Step> steps = new ArrayList<>();
        XrayTestExecutionImport.TestRun.Step step1 = new XrayTestExecutionImport.TestRun.Step();
        step1.setStatus(XrayTestExecutionImport.TestRun.Status.PASS);
        steps.add(step1);
        XrayTestExecutionImport.TestRun.Step step2 = new XrayTestExecutionImport.TestRun.Step();
        step2.setStatus(XrayTestExecutionImport.TestRun.Status.PASS);
        steps.add(step1);
        run1.setSteps(steps);

        runMap.put(run1.getTestKey(), run1);

        // Test run 2 with passed and failed steps
        XrayTestExecutionImport.TestRun run2 = new XrayTestExecutionImport.TestRun("SWFTE-145");
        run2.setStatus(XrayTestExecutionImport.TestRun.Status.FAIL);
        run2.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
        run2.getTestInfo().setSummary("testMapperTestStepsFailed");

        steps = new ArrayList<>();
        step1 = new XrayTestExecutionImport.TestRun.Step();
        step1.setStatus(XrayTestExecutionImport.TestRun.Status.PASS);
        steps.add(step1);
        step2 = new XrayTestExecutionImport.TestRun.Step();
        step2.setStatus(XrayTestExecutionImport.TestRun.Status.FAIL);
        steps.add(step2);
        run2.setSteps(steps);

        runMap.put(run2.getTestKey(), run2);

//        XrayTestExecutionImport.TestRun run3 = new XrayTestExecutionImport.TestRun("SWFTE-133");
//        run3.setStatus(XrayTestExecutionImport.TestRun.Status.SKIPPED);
//        run3.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
//        run3.getTestInfo().setSummary("testMapperSkips");
//        runMap.put(run3.getTestKey(), run3);

        return runMap;
    }


}
