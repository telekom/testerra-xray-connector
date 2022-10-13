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

import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.JqlQuery;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueType;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.IssueTypeEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.ProjectEquals;
import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.SummaryContainsExact;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraNameReference;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.XrayUtils;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.net.URISyntaxException;

public class AbstractTest extends TesterraTest implements Loggable {
    protected WebResource webResource;

    @BeforeTest
    public void prepareWebResource() throws URISyntaxException {
        webResource = TestUtils.prepareWebResource("xray.properties");
    }

    protected XrayTestExecutionIssue generateNewTestExecution(String project, String summary, String description, String revision) {
        XrayTestExecutionIssue issue = new XrayTestExecutionIssue();
        issue.setProject(new JiraNameReference(project));
        issue.getProject().setKey(project);
        issue.setSummary(summary);
        issue.setDescription(description);
        issue.setRevision(revision);

        return issue;
    }

    protected XrayTestIssue generateNewTestIssue(String project, String summary, String description) {
        XrayTestIssue issue = new XrayTestIssue();
        issue.setProject(new JiraNameReference(project));
        issue.getProject().setKey(project);
        issue.setSummary(summary);
        issue.setDescription(summary);
        return issue;
    }

    protected JqlQuery getTestExecutionJqlQuery(String summary) {
        return JqlQuery.create()
                .addCondition(new ProjectEquals(XrayConfig.getInstance().getProjectKey()))
                .addCondition(new IssueTypeEquals(IssueType.TestExecution.getIssueType()))
                .addCondition(new SummaryContainsExact(summary))
                .build();
    }

    protected XrayTestIssue getXrayTestIssueByKey(XrayUtils utils, String key) {
        try {
            return utils.getIssue(key, XrayTestIssue::new);
        } catch (IOException e) {
            Assert.fail("Cannot read XrayTestIssue " + key);
            log().error(e.getMessage());
        }
        return null;
    }

}
