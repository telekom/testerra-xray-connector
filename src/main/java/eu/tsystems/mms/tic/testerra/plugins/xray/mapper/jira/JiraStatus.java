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

package eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira;

import java.util.Map;

public class JiraStatus extends JiraNameReference {
    private JiraStatusCategory statusCategory;
    private String description;

    public JiraStatus(Map<String, Object> map) {
        super(map);
        this.description = (String)map.getOrDefault("description", null);
        if (map.containsKey("statusCategory")) {
            this.statusCategory = new JiraStatusCategory((Map<String, Object>)map.get("statusCategory"));
        }
    }

    public JiraStatus() {
        super();
    }

    public JiraStatus(String name) {
        super(name);
    }

    public JiraStatusCategory getStatusCategory() {
        return statusCategory;
    }

    public void setStatusCategory(JiraStatusCategory statusCategory) {
        this.statusCategory = statusCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
