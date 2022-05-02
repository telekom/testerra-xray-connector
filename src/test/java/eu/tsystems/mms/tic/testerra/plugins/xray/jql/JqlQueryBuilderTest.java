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

package eu.tsystems.mms.tic.testerra.plugins.xray.jql;

import static org.testng.Assert.assertEquals;


import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.Operator;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.Fields;
import org.testng.annotations.Test;

public class JqlQueryBuilderTest extends AbstractTest {

    @Test
    public void testBuildMultipleConditions() throws Exception {
        final JqlQuery query = JqlQuery.create()
                .addCondition(new DefaultJqlCondition("summary", Operator.Equals, new SingleValue("bli")))
                .addCondition(new DefaultJqlCondition("description", Operator.Contains, new SingleValue("bla")))
                .addCondition(new DefaultJqlCondition(Fields.TEST_EXECUTION_REVISION.getJQLTerm(), Operator.Contains, new SingleValue("blubb")))
                .build();
        assertEquals(query.createJql(), "summary = bli AND description ~ bla AND cf[14272] ~ blubb");
    }

    @Test
    public void testBuildSingleValueContainsSpaces() throws Exception {
        final JqlQuery query = JqlQuery.create()
                .addCondition(new DefaultJqlCondition("summary", Operator.Equals, new SingleValue("bli bla blubb")))
                .build();
        assertEquals(query.createJql(), "summary = \"bli bla blubb\"");
    }

    @Test
    public void testBuildExcactValueContainsExtraHyphens() throws Exception {
        final JqlQuery query = JqlQuery.create()
                .addCondition(new DefaultJqlCondition("summary", Operator.Equals, new ExactSingleValue("bli bla blubb")))
                .build();
        assertEquals(query.createJql(), "summary = \"\\\"bli bla blubb\\\"\"");
    }

    @Test
    public void testCombineQueries() throws Exception {
        final JqlQuery firstQuery = JqlQuery.create()
                .addCondition(new DefaultJqlCondition("summary", Operator.Equals, new SingleValue("bli bla blubb")))
                .build();
        final JqlQuery secondQuery = JqlQuery.create()
                .addCondition(new DefaultJqlCondition("description", Operator.Contains, new SingleValue("bla")))
                .build();
        final JqlQuery combinedQuery = JqlQuery.combine(firstQuery, secondQuery);
        assertEquals(combinedQuery.createJql(), "summary = \"bli bla blubb\" AND description ~ bla");
    }
}
