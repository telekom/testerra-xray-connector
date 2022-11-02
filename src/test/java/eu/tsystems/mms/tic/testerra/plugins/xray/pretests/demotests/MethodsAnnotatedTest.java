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
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class MethodsAnnotatedTest extends AbstractTestBase {

    @Override
    @Test()
    @XrayTest(key = "SWFTE-123")
    public void testMapperPasses() {
        super.testMapperPasses();
    }

    @Override
    @Test()
    @XrayTest(key = "SWFTE-125")
    public void testMapperFails() {
        super.testMapperFails();
    }

    @Override
    @Test(dependsOnMethods = "testMapperFails")
    @XrayTest(key = "SWFTE-133")
    public void testMapperSkips() {
        super.testMapperSkips();
    }

    public static List<XrayTestExecutionImport.TestRun> getExpectedTestRuns() {

        List<XrayTestExecutionImport.TestRun> testRunList = new ArrayList<>();

        XrayTestExecutionImport.TestRun run1 = new XrayTestExecutionImport.TestRun("SWFTE-123");
        run1.setStatus(XrayTestExecutionImport.TestRun.Status.PASS);
        run1.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
        run1.getTestInfo().setSummary("EmptyMapper_Passes");
        run1.getTestInfo().setDescription("EmptyMapper_Passes");
        testRunList.add(run1);

        XrayTestExecutionImport.TestRun run2 = new XrayTestExecutionImport.TestRun("SWFTE-125");
        run2.setStatus(XrayTestExecutionImport.TestRun.Status.FAIL);
        run2.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
        run2.getTestInfo().setSummary("EmptyMapper_Fails");
        run2.getTestInfo().setDescription("EmptyMapper_Fails");
        testRunList.add(run2);

        XrayTestExecutionImport.TestRun run3 = new XrayTestExecutionImport.TestRun("SWFTE-133");
        run3.setStatus(XrayTestExecutionImport.TestRun.Status.SKIPPED);
        run3.setTestInfo(new XrayTestExecutionImport.TestRun.Info());
        run3.getTestInfo().setSummary("EmptyMapper_Skips");
        run3.getTestInfo().setDescription("EmptyMapper_Skips");
        testRunList.add(run3);

        return testRunList;
    }

}
