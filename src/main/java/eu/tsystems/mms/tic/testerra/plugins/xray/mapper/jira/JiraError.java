/*
 * Testerra Xray-Connector
 *
 * (C) 2022, Martin Großmann, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 2022-10-11
 *
 * @author mgn
 */
public class JiraError {

    private String[] messages;
    private String summary;
    private String project;

    public List<String> getMessages() {
        return Arrays.asList(messages);
    }

    public String getSummary() {
        return summary;
    }

    public String getProject() {
        return project;
    }
}
