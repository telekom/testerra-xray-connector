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

package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.mapper;

import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.SummaryContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestSetIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayMapper;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import org.testng.ITestClass;
import org.testng.ITestResult;

public class ResultMapper implements XrayMapper {
    private static final String TEST_SET_SUMMARY = "TSA - all Tests under Test";
    //    @Override
    //    public JqlQuery methodToXrayTest(ITestNGMethod testNgMethod) {
    //        return JqlQuery.create()
    //                .addCondition(new SummaryContainsExact(testNgMethod.getMethodName()))
    //                .build();
    //    }

    @Override
    public JqlQuery resultToXrayTest(ITestResult testNgResult) {
        final Object[] parameters = testNgResult.getParameters();
        if (parameters.length > 0) {
            final String summary = String.format("%s with %s", testNgResult.getMethod().getMethodName(), parameters[0]);
            return JqlQuery.create()
                    .addCondition(new SummaryContainsExact(summary))
                    .build();
        } else {
            return JqlQuery.create()
                    .addCondition(new SummaryContainsExact(testNgResult.getMethod().getMethodName()))
                    .build();

        }
    }

    @Override
    public JqlQuery classToXrayTestSet(ITestClass testNgClass) {
        return JqlQuery.create()
                .addCondition(new SummaryContainsExact(TEST_SET_SUMMARY))
                .build();
    }

    @Override
    public void updateTestSet(XrayTestSetIssue xrayTestSetIssue, ClassContext classContext) {
        xrayTestSetIssue.setSummary(TEST_SET_SUMMARY);
    }
}
