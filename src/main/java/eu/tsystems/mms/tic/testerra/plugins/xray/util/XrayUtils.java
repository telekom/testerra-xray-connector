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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.FreshXrayTestExecution;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.UpdateXrayTestExecution;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecution;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestStatus;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.NotSyncableException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class XrayUtils {

    private static final String IMPORT_EXECUTION_PATH = "raven/1.0/import/execution";
    private static final String EXECUTION_RESULT_PATH = "raven/1.0/execution/result";
    private static Logger logger = LoggerFactory.getLogger(XrayUtils.class);

    private XrayUtils() {
    }

    public static String syncTestExecutionReturnKey(final WebResource webResource, final XrayTestExecution testExecution)
            throws IOException, NotSyncableException {
        final String response = syncTestExecReturnResponse(webResource, testExecution);
        final JsonNode jsonNode = new ObjectMapper().readTree(response);
        final JsonNode foundKey = jsonNode.findValue("key");
        if (foundKey != null) {
            return foundKey.asText();
        } else {
            throw new NotSyncableException("no key found in server response");
        }
    }

    public static String syncTestExecReturnResponse(final WebResource webResource, final XrayTestExecution testExecution)
            throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final String string = objectMapper.writeValueAsString(testExecution);
        try {
            return webResource.path(IMPORT_EXECUTION_PATH)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(string)
                    .post(String.class);
        } catch (final UniformInterfaceException e) {
            logger.error(String.format("error POSTing request: %s", string), e);
            throw new NotSyncableException(e);
        }
    }

    public static UpdateXrayTestExecution createUpdateTestExecution(final String issueKey, final Iterable<String> testKeys) {
        final UpdateXrayTestExecution execution = new UpdateXrayTestExecution(issueKey);
        final XrayInfo xrayInfo = new XrayInfo();
        xrayInfo.setStartDate(new Date());
        xrayInfo.setFinishDate(new Date());
        execution.setInfo(xrayInfo);
        execution.setTests(keysToXrayTestWithTodoStatus(testKeys));
        return execution;
    }

    public static FreshXrayTestExecution createFreshTestExecution(XrayInfo xrayInfo, final Iterable<String> testKeys) {
        final FreshXrayTestExecution execution = new FreshXrayTestExecution();
        execution.setInfo(xrayInfo);
        execution.setTests(keysToXrayTestWithTodoStatus(testKeys));
        return execution;
    }

    private static LinkedHashSet<XrayTestIssue> keysToXrayTestWithTodoStatus(final Iterable<String> testKeys) {
        final LinkedHashSet<XrayTestIssue> xrayTestIssues = new LinkedHashSet<>();
        for (final String testKey : testKeys) {
            final XrayTestIssue xrayTestIssue = new XrayTestIssue();
            xrayTestIssue.setTestKey(testKey);
            xrayTestIssue.setStatus(XrayTestStatus.TODO);
            xrayTestIssues.add(xrayTestIssue);
        }
        return xrayTestIssues;
    }

    public static LinkedHashSet<XrayTestIssue> getTestsFromExecution(final WebResource webResource, final String issueKey) throws IOException {
        final String result = webResource.path(EXECUTION_RESULT_PATH).queryParam("testExecKey", issueKey).get(String.class);
        final ObjectMapper objectMapper = new ObjectMapper();
        final XrayTestIssue[] testIssues = objectMapper.readValue(result, XrayTestIssue[].class);
        return Sets.newLinkedHashSet(Arrays.asList(testIssues));
    }

    public static String exportTestExecutionAsJson(final WebResource webResource, final String issueKey) {
        return webResource.path(EXECUTION_RESULT_PATH)
                .queryParam("testExecKey", issueKey)
                .get(String.class);
    }

}
