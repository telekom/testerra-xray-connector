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

package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.predef.SetAffectedVersions;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.update.predef.SetLabels;
import org.testng.Assert;
import org.testng.annotations.Test;

@Deprecated
public class JiraIssueUpdateTest {

    @Test
    public void testCreate01() throws Exception {
        final JiraIssueUpdate jiraIssueUpdate = JiraIssueUpdate.create()
                .field("summary", JiraVerb.SET, new JiraString("bla"))
                .build();
        final String s = new ObjectMapper().writeValueAsString(jiraIssueUpdate);
        Assert.assertEquals(s, "{\"update\":{\"summary\":[{\"set\":\"bla\"}]}}");
    }

    @Test
    public void testCreate02() throws Exception {
        final JiraIssueUpdate jiraIssueUpdate = JiraIssueUpdate.create()
                .field("priority", JiraVerb.SET, new JiraNameValue("Kritisch"))
                .build();
        final String s = new ObjectMapper().writeValueAsString(jiraIssueUpdate);
        Assert.assertEquals(s, "{\"update\":{\"priority\":[{\"set\":{\"name\":\"Kritisch\"}}]}}");
    }

    @Test
    public void testCreate03() throws Exception {
        final JiraIssueUpdate jiraIssueUpdate = JiraIssueUpdate.create()
                .field("labels", JiraVerb.SET, new JiraArray("Test-Automatisierung", "Aufräumaktion"))
                .build();
        final String s = new ObjectMapper().writeValueAsString(jiraIssueUpdate);
        Assert.assertEquals(s, "{\"update\":{\"labels\":[{\"set\":[\"Test-Automatisierung\",\"Aufräumaktion\"]}]}}");
    }

    @Test
    public void testCreate04() throws Exception {
        final JiraIssueUpdate jiraIssueUpdate = JiraIssueUpdate.create()
                .field("versions", JiraVerb.SET, new JiraNameValueArray("1.0.2", "1.0.3"))
                .build();
        final String s = new ObjectMapper().writeValueAsString(jiraIssueUpdate);
        Assert.assertEquals(s, "{\"update\":{\"versions\":[{\"set\":[{\"name\":\"1.0.2\"},{\"name\":\"1.0.3\"}]}]}}");
    }

    @Test
    public void testCreate05() throws Exception {
        final JiraIssueUpdate jiraIssueUpdate = JiraIssueUpdate.create()
                .field(new SetAffectedVersions("1.0.2", "1.0.3"))
                .build();
        final String s = new ObjectMapper().writeValueAsString(jiraIssueUpdate);
        Assert.assertEquals(s, "{\"update\":{\"versions\":[{\"set\":[{\"name\":\"1.0.2\"},{\"name\":\"1.0.3\"}]}]}}");
    }

    @Test
    public void testCreate06() throws Exception {
        final JiraIssueUpdate jiraIssueUpdate = JiraIssueUpdate.create()
                .field(new SetLabels("Test-Automatisierung"))
                .field(new SetAffectedVersions("1.0.2", "1.0.3"))
                .build();
        final String s = new ObjectMapper().writeValueAsString(jiraIssueUpdate);
        Assert.assertEquals(s, "{\"update\":{\"versions\":[{\"set\":[{\"name\":\"1.0.2\"},{\"name\":\"1.0.3\"}]}]," +
                "\"labels\":[{\"set\":[\"Test-Automatisierung\"]}]}}");
    }

}
