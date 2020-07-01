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

package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import org.testng.ITestClass;
import org.testng.ITestResult;


public interface XrayMapper {

    /**
     * called for matching Test method against Xray Test
     * create a Jql-Query that is able to retrieve a single Xray-Test
     * 'project = $projektKey AND issuetype = Test AND key in testSetTests($testSetKey)' is prepended automatically
     * return null if no automatic matching is required
     *
     * @param testNgResult the test result
     * @return JqlQuery that is able to match a single Xray-Test or null
     */
    JqlQuery resultToXrayTest(ITestResult testNgResult);

    /**
     * called for matching Test class against Xray TestSet
     * create a Jql-Query that is able to retrieve a single Xray-TestSet
     * 'project = $projektKey AND issuetype = "Test Set"' is prepended automatically
     * return null if no automatic matching is required
     *
     * @param testNgClass test class
     * @return JqlQuery that is able to match a single Xray-TestSet or null
     */
    JqlQuery classToXrayTestSet(ITestClass testNgClass);

}
