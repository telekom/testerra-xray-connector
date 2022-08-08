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
package eu.tsystems.mms.tic.testerra.plugins.xray.connect;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.filter.GetRequestOnlyFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.connect.filter.LoggingFilter;
import eu.tsystems.mms.tic.testerra.plugins.xray.util.JiraUtils;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.utils.ProxyUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URL;

public class XrayConnector implements Loggable {

    private final WebResource webResource;

    public XrayConnector() {
        final Client client;
        URL proxyUrl = ProxyUtils.getSystemHttpsProxyUrl();
        if (proxyUrl != null && StringUtils.isNotBlank(proxyUrl.getHost()) && proxyUrl.getPort() != -1) {
            client = RESTClientFactory.createWithProxy(proxyUrl);
        } else {
            client = RESTClientFactory.createDefault();
        }

        XrayConfig xrayConfig = XrayConfig.getInstance();
        webResource = client.resource(xrayConfig.getRestServiceUri());

        if (StringUtils.isNotEmpty(xrayConfig.getToken())) {
            log().info("Use Bearer token authentication");
            webResource.addFilter(new HttpBearerTokenAuthFilter(xrayConfig.getToken()));
        } else {
            log().info("Use Basic authentication");
            webResource.addFilter(new HTTPBasicAuthFilter(xrayConfig.getUsername(), xrayConfig.getPassword()));
        }

        if (xrayConfig.isWebResourceFilterGetRequestsOnlyEnabled()) {
            webResource.addFilter(new GetRequestOnlyFilter());
        }

        if (xrayConfig.isWebResourceFilterLoggingEnabled()) {
            webResource.addFilter(new LoggingFilter());
        }
    }

    public void uploadTestExecutionAttachment(final String issueKey, final InputStream is, final String fileName) {
        JiraUtils.uploadAttachment(webResource, issueKey, is, fileName);
    }

    public WebResource getWebResource() {
        return this.webResource;
    }
}
