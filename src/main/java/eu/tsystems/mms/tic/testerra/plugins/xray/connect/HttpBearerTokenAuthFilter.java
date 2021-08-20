/*
 * Testerra Xray-Connector
 *
 * (C) 2021, Martin Großmann, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class HttpBearerTokenAuthFilter extends ClientFilter {

    private final String authentication;

    public HttpBearerTokenAuthFilter(String token) {
        this.authentication = "Bearer " + token;
    }

    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        if (!cr.getHeaders().containsKey("Authorization")) {
            cr.getHeaders().add("Authorization", this.authentication);
        }

        return this.getNext().handle(cr);
    }
}
