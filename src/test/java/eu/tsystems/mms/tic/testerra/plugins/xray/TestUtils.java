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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.RESTClientFactory;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.filter.GetRequestOnlyFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.filter.LoggingFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.jira.JiraIssue;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.JiraUtils;
import java.text.ParseException;
import java.util.Date;

public class TestUtils {

    public static Date getDateFromField(final JiraIssue issue, final String fieldName) throws ParseException {
        final Object foundFieldValue = issue.getFields().get(fieldName);
        if (foundFieldValue != null) {
            return JiraUtils.DATE_FORMAT.parse(foundFieldValue.toString());
        } else {
            return null;
        }
    }

    public static WebResource prepareWebResource(String configFileName) {
        final Client client = RESTClientFactory.createDefault();

        XrayConfig.reset();
        XrayConfig.init(configFileName);
        XrayConfig xrayConfig = XrayConfig.getInstance();
        WebResource webResource = client.resource(xrayConfig.getRestServiceUri());
        webResource.addFilter(new HTTPBasicAuthFilter(XrayConfig.getInstance().getUsername(), XrayConfig.getInstance().getPassword()));
        if (xrayConfig.isWebResourceFilterGetRequestsOnlyEnabled()) {
            webResource.addFilter(new GetRequestOnlyFilter());
        }
        if (xrayConfig.isWebResourceFilterLoggingEnabled()) {
            webResource.addFilter(new LoggingFilter());
        }
        return webResource;
    }

}
