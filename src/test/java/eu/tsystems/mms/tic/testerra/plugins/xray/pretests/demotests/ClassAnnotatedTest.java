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
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassAnnotatedTest extends AbstractTestBase {

    @Test()
//    @XrayTest(key = "SWFTE-134")
    public void testSummaryMapperPasses() {
        super.testMapperPasses();
    }

    @Test()
//    @XrayTest(key = "SWFTE-137")
    public void testSummaryMapperFails() {
        super.testMapperFails();
    }

    @Test(dependsOnMethods = "testSummaryMapperFails")
//    @XrayTest(key = "SWFTE-138")
    public void testSummaryMapperSkips() {
        super.testMapperSkips();
    }

    public static Map<String, XrayTestExecutionImport.TestRun> getExpectedTestRuns() {

        Map<String, XrayTestExecutionImport.TestRun> runMap = new HashMap<>();

        XrayTestExecutionImport.TestRun run1 = new XrayTestExecutionImport.TestRun("SWFTE-134");
        run1.setStatus(XrayTestExecutionImport.TestRun.Status.PASS);
        run1.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
        run1.getTestInfo().setSummary("testSummaryMapperPasses");
        runMap.put(run1.getTestKey(), run1);

        XrayTestExecutionImport.TestRun run2 = new XrayTestExecutionImport.TestRun("SWFTE-137");
        run2.setStatus(XrayTestExecutionImport.TestRun.Status.FAIL);
        run2.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
        run2.getTestInfo().setSummary("testSummaryMapperFails");
        runMap.put(run2.getTestKey(), run2);

        XrayTestExecutionImport.TestRun run3 = new XrayTestExecutionImport.TestRun("SWFTE-138");
        run3.setStatus(XrayTestExecutionImport.TestRun.Status.SKIPPED);
        run3.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
        run3.getTestInfo().setSummary("testSummaryMapperSkips");
        runMap.put(run3.getTestKey(), run3);

        return runMap;
    }

}
