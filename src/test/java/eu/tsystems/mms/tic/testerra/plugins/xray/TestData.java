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

package eu.tsystems.mms.tic.testerra.plugins.xray;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class TestData {

    public static final String TEST_EXEC_KEY_DEFAULT = "SWFTE-800";
    public static final String TEST_EXEC_KEY_MULTIPLE = "SWFTE-801";

    public static final String TEST_EXEC_REVISION_DEFAULT = "2017-04-10_default";
    public static final String TEST_EXEC_REVISION_MULTIPLE = "2017-04-10_multiple";

    public static final String TEST_EXEC_SUMMARY_DEFAULT = "Simulated Run Execution";
    public static final String TEST_EXEC_SUMMARY_MULTIPLE = "Simulated Multiple Matches Execution";
    public static final List<String> TEST_EXEC_TEST_ENVIRONMENTS_DEFAULT = ImmutableList.of("Bli", "Bla", "Blubb");

}
