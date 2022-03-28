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

package eu.tsystems.mms.tic.testerra.plugins.xray.util;

import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class XrayUtils extends JiraUtils {

    private static final String IMPORT_EXECUTION_PATH = "raven/1.0/import/execution";
    private static final String EXECUTION_RESULT_PATH = "raven/1.0/execution/result";

    public static final String PREFIX_NEW_ISSUE = "_NEW_";

    public XrayUtils(WebResource webResource) {
        super(webResource);
    }

    public void importTestExecution(XrayTestExecutionImport testExecutionImport) throws IOException {

        testExecutionImport.getTests().forEach(testRun -> {
            if (testRun.getTestKey() != null && testRun.getTestKey().contains(PREFIX_NEW_ISSUE)) {
                testRun.setTestKey(null);
            }
        });
        Optional<String> post = post(IMPORT_EXECUTION_PATH, testExecutionImport);
        if (post.isPresent()) {
            XrayTestExecutionImport.Result xrayTestExecutionResult = getObjectMapper().readValue(post.get(), XrayTestExecutionImport.Result.class);
            testExecutionImport.setTestExecutionKey(xrayTestExecutionResult.getTestExecIssue().getKey());
            testExecutionImport.setResultTestIssueImport(xrayTestExecutionResult.getTestIssues());
        }
    }

    public Set<XrayTestExecutionImport.TestRun> getTestRunsByTestExecutionKey(String issueKey) throws IOException {
        String jsonResponse = getWebResource()
                .path(EXECUTION_RESULT_PATH)
                .queryParam("testExecKey", issueKey)
                .get(String.class);
        XrayTestExecutionImport.TestRun[] testRuns = getObjectMapper().readValue(jsonResponse, XrayTestExecutionImport.TestRun[].class);
        return Arrays.stream(testRuns).collect(Collectors.toSet());
    }

    public String exportTestExecutionAsJson(final WebResource webResource, final String issueKey) {
        return webResource.path(EXECUTION_RESULT_PATH)
                .queryParam("testExecKey", issueKey)
                .get(String.class);
    }

}
