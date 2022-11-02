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

package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.pretests.demotests.AbstractTestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

@XrayTestSet(key = "SWFTE-8")
public class ClassAnnotatedWithKeyAndMultipleMethodMatchesTest extends AbstractTestBase {

    @Test
    public void matchesMultiple() {
        Assert.assertTrue(true);
    }

    @Override
    @Test
    public void testMapperPasses() {
        super.testMapperPasses();
    }

    @Override
    @Test
    public void testMapperFails() {
        super.testMapperFails();
    }

    @Override
    @Test(dependsOnMethods = "testMapperFails")
    public void testMapperSkips() {
        super.testMapperSkips();
    }

}
