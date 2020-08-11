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
import eu.tsystems.mms.tic.testerra.plugins.xray.logging.LoggerUtils;
import eu.tsystems.mms.tic.testframework.logging.LogLevel;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4JLoggingFilter extends ClientFilter {

    private static final String REQUEST_PREFIX = "> ";

    private static final String RESPONSE_PREFIX = "< ";

    private static final String NOTIFICATION_PREFIX = "* ";
    private final int maxEntitySize = 10 * 1024;
    private final LogLevel logLevel;
    private long id = 0;
    private Logger logger = LoggerFactory.getLogger(Slf4JLoggingFilter.class);

    public Slf4JLoggingFilter(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    private void log(StringBuilder b) {
        LoggerUtils.logWithLogLevel(logger, logLevel, b.toString());
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        long id = ++this.id;
        logRequest(id, request);
        ClientResponse response = getNext().handle(request);
        logResponse(id, response);
        return response;
    }

    //    private void logRequest(long id, ClientRequest request) {
    //        StringBuilder b = new StringBuilder();
    //
    //        printRequestLine(b, id, request);
    //        printRequestHeaders(b, id, request.getHeaders());
    //
    //        if (request.getEntity() != null) {
    //            request.setAdapter(new Adapter(request.getAdapter(), b));
    //        } else {
    //            log(b);
    //        }
    //    }
    private void logRequest(long id, ClientRequest request) {
        StringBuilder b = new StringBuilder();

        printRequestLine(b, id, request);
        printRequestHeaders(b, id, request.getHeaders());
        printRequestEntity(b, id, request.getEntity());

        //        if (request.getEntity() != null) {
        //            request.setAdapter(new Adapter(request.getAdapter(), b));
        //        } else {
        //            log(b);
        //        }
        log(b);
    }

    private void printRequestEntity(StringBuilder b, long id, Object entity) {
        if (entity != null) {
            prefixId(b, id).append(REQUEST_PREFIX).append("\n").append(entity).append("\n");
        }
    }

    private StringBuilder prefixId(StringBuilder b, long id) {
        b.append(Long.toString(id)).append(" ");
        return b;
    }

    private void printRequestLine(StringBuilder b, long id, ClientRequest request) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client out-bound request").append("\n");
        prefixId(b, id).append(REQUEST_PREFIX).append(request.getMethod()).append(" ").
                append(request.getURI().toASCIIString()).append("\n");
    }

    private void printRequestHeaders(StringBuilder b, long id, MultivaluedMap<String, Object> headers) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            List<Object> val = e.getValue();
            String header = e.getKey();

            if (val.size() == 1) {
                prefixId(b, id).append(REQUEST_PREFIX).append(header).append(": ").append(ClientRequest.getHeaderValue(val.get(0))).append("\n");
            } else {
                StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (Object o : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    sb.append(ClientRequest.getHeaderValue(o));
                }
                prefixId(b, id).append(REQUEST_PREFIX).append(header).append(": ").append(sb.toString()).append("\n");
            }
        }
    }

    private void printEntity(StringBuilder b, byte[] entity) {
        if (entity.length == 0) {
            return;
        }
        b.append(new String(entity)).append("\n");
    }

    private void logResponse(long id, ClientResponse response) {
        StringBuilder b = new StringBuilder();

        printResponseLine(b, id, response);
        printResponseHeaders(b, id, response.getHeaders());

        InputStream stream = response.getEntityInputStream();
        try {
            if (!response.getEntityInputStream().markSupported()) {
                stream = new BufferedInputStream(stream);
                response.setEntityInputStream(stream);
            }

            stream.mark(maxEntitySize + 1);
            byte[] entity = new byte[maxEntitySize + 1];
            int entitySize = stream.read(entity);

            if (entitySize > 0) {
                b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize)));
                if (entitySize > maxEntitySize) {
                    b.append("...more...");
                }
                b.append('\n');
                stream.reset();
            }
        } catch (IOException ex) {
            throw new ClientHandlerException(ex);
        }
        log(b);
    }

    private void printResponseLine(StringBuilder b, long id, ClientResponse response) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).
                append("Client in-bound response").append("\n");
        prefixId(b, id).append(RESPONSE_PREFIX).
                append(Integer.toString(response.getStatus())).
                append("\n");
    }

    private void printResponseHeaders(StringBuilder b, long id, MultivaluedMap<String, String> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String header = e.getKey();
            for (String value : e.getValue()) {
                prefixId(b, id).append(RESPONSE_PREFIX).append(header).append(": ").
                        append(value).append("\n");
            }
        }
        prefixId(b, id).append(RESPONSE_PREFIX).append("\n");
    }

    //    private final class Adapter extends AbstractClientRequestAdapter {
    //        private final StringBuilder b;
    //
    //        Adapter(ClientRequestAdapter cra, StringBuilder b) {
    //            super(cra);
    //            this.b = b;
    //        }
    //
    //        public OutputStream adapt(ClientRequest request, OutputStream out) throws IOException {
    //            return new LoggingOutputStream(getAdapter().adapt(request, out), b);
    //        }
    //
    //    }

    //    private final class LoggingOutputStream extends OutputStream {
    //        private final OutputStream out;
    //
    //        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //
    //        private final StringBuilder b;
    //
    //        LoggingOutputStream(OutputStream out, StringBuilder b) {
    //            this.out = out;
    //            this.b = b;
    //        }
    //
    //        @Override
    //        public void write(byte[] b) throws IOException {
    //            baos.write(b);
    //            out.write(b);
    //        }
    //
    //        @Override
    //        public void write(byte[] b, int off, int len) throws IOException {
    //            baos.write(b, off, len);
    //            out.write(b, off, len);
    //        }
    //
    //        @Override
    //        public void write(int b) throws IOException {
    //            baos.write(b);
    //            out.write(b);
    //        }
    //
    //        @Override
    //        public void close() throws IOException {
    //            printEntity(b, baos.toByteArray());
    //            log(b);
    //            out.close();
    //        }
    //    }
}
