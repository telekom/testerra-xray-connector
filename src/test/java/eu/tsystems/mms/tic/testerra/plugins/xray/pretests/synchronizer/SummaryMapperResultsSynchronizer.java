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

package eu.tsystems.mms.tic.testerra.plugins.xray.pretests.synchronizer;

import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.AbstractXrayResultsSynchronizer;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.DefaultSummaryMapper;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayMapper;
import eu.tsystems.mms.tic.testframework.report.model.context.ExecutionContext;

public class SummaryMapperResultsSynchronizer extends AbstractXrayResultsSynchronizer {

    public static final String EXECUTION_SUMMARY1 = "XrayDefaultSummeryMapperTest";
    public static final String EXECUTION_SUMMARY2 = "XrayEmtpyMapperTestWithStepsWithoutKey";
    private String executionSummary;

    public SummaryMapperResultsSynchronizer(final String executionSummary) {
        this.executionSummary = executionSummary;
    }

    public XrayMapper getXrayMapper() {
        return new DefaultSummaryMapper() {
            @Override
            public void updateTestExecution(XrayTestExecutionIssue xrayTestExecutionIssue, ExecutionContext executionContext) {
                xrayTestExecutionIssue.setSummary(executionSummary);
            }
        };
    }
}
