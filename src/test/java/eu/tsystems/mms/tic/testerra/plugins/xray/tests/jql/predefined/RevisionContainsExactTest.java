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

package eu.tsystems.mms.tic.testerra.plugins.xray.tests.jql.predefined;

import static org.testng.Assert.assertEquals;


import eu.tsystems.mms.tic.testerra.plugins.xray.tests.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.RevisionContainsExact;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RevisionContainsExactTest extends AbstractTest {

    @Test
    public void testCreateJql() throws Exception {
        Assert.assertEquals(new RevisionContainsExact("RC6.5.0_129094").createJql(), "cf[14272] ~ \\\"RC6.5.0_129094\\\"");
    }
}
