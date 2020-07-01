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

package eu.tsystems.mms.tic.testerra.plugins.xray.connect.filter;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.MessageBodyWorkers;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRequestOnlyFilter extends ClientFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        switch (cr.getMethod()) {
            case "GET":
                return getNext().handle(cr);
            case "POST":
                final String schemeSpecificPart = cr.getURI().getSchemeSpecificPart();
                if (schemeSpecificPart.endsWith("import/execution") || schemeSpecificPart.endsWith("/jira/rest/api/2/issue")) {
                    /* creating execution via xray import */
                    // example: 200 {"testExecIssue":{"id":"311360","key":"SWFTE-224","self":"https://projectcenter.t-systems-mms.eu/jira/rest/api/2/issue/311360"}}
                    final InBoundHeaders inBoundHeaders = new InBoundHeaders();
                    inBoundHeaders.add("Content-Type", MediaType.APPLICATION_JSON);
                    final byte[] fakeBytes =
                            String.format("{\"testExecIssue\":{\"key\":\"%s\"}}", XrayConfig.getInstance().getFakeTestExecutionKey()).getBytes();
                    ClientResponse fake = new ClientResponse(ClientResponse.Status.OK, new InBoundHeaders(),
                            new ByteArrayInputStream(fakeBytes), new MockMessageBodyWorkers());
                    return fake;
                } else if (schemeSpecificPart.endsWith("/jira/rest/api/2/issue")) {
                    /* creating execution via generic jira post */
                    // example: {"id": "331937", "key": "SWFTE-338", "self": "https://projectcenter.t-systems-mms.eu/jira/rest/api/2/issue/331937"}
                    final InBoundHeaders inBoundHeaders = new InBoundHeaders();
                    inBoundHeaders.add("Content-Type", MediaType.APPLICATION_JSON);
                    final byte[] fakeBytes = String.format("{\"key\":\"%s\"}", XrayConfig.getInstance().getFakeTestExecutionKey()).getBytes();
                    ClientResponse fake = new ClientResponse(ClientResponse.Status.OK, new InBoundHeaders(),
                            new ByteArrayInputStream(fakeBytes), new MockMessageBodyWorkers());
                    return fake;
                } else if (schemeSpecificPart.endsWith("transitions")) {
                    ClientResponse fake = new ClientResponse(ClientResponse.Status.OK, new InBoundHeaders(),
                            new ByteArrayInputStream(new byte[] {}), new MockMessageBodyWorkers());
                    return fake;
                }
            case "PUT":
                ClientResponse fake = new ClientResponse(ClientResponse.Status.OK, new InBoundHeaders(),
                        new ByteArrayInputStream(new byte[] {}), new MockMessageBodyWorkers());
                return fake;
            default:
                throw new WebApplicationException();
        }
    }

    private static class MockMessageBodyWorkers implements MessageBodyWorkers {

        @Override
        public Map<MediaType, List<MessageBodyReader>> getReaders(MediaType mediaType) {
            return null;
        }

        @Override
        public Map<MediaType, List<MessageBodyWriter>> getWriters(MediaType mediaType) {
            return null;
        }

        @Override
        public String readersToString(Map<MediaType, List<MessageBodyReader>> readers) {
            return null;
        }

        @Override
        public String writersToString(Map<MediaType, List<MessageBodyWriter>> writers) {
            return null;
        }

        @Override
        public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return new MessageBodyReader<T>() {
                @Override
                public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
                    return true;
                }

                @Override
                public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                  MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws
                        IOException, WebApplicationException {
                    return (T) IOUtils.toString(entityStream);
                }
            };
        }

        @Override
        public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return null;
        }

        @Override
        public <T> List<MediaType> getMessageBodyWriterMediaTypes(Class<T> type, Type genericType, Annotation[] annotations) {
            return null;
        }

        @Override
        public <T> MediaType getMessageBodyWriterMediaType(Class<T> type, Type genericType, Annotation[] annotations,
                                                           List<MediaType> acceptableMediaTypes) {
            return null;
        }
    }
}
