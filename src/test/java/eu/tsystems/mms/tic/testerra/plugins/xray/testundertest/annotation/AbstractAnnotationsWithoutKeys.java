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

package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testframework.report.model.steps.TestStep;
import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@XrayTestSet
public abstract class AbstractAnnotationsWithoutKeys extends TesterraTest {

    @Test
    @XrayTest()
    public void passes() {
        Assert.assertTrue(true);
    }

    @Test
    @XrayTest()
    public void fails() {
        Assert.assertTrue(false);
    }

    @Test(dependsOnMethods = "fails")
    @XrayTest()
    public void skips() {
        Assert.assertTrue(true);
    }

    @Test
    @XrayTest
    public void testWithSteps() {
        TestStep.begin("First step");
        TestStep.begin("Second step");
    }
}