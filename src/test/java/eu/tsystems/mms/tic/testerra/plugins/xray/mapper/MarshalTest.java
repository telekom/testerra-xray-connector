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

package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.tsystems.mms.tic.testerra.plugins.xray.AbstractTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayTestExecutionImport;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MarshalTest extends AbstractTest {


    @Test
    public void testMarshalTestToJson() throws JsonProcessingException {

        final XrayTestExecutionImport.Test test = new XrayTestExecutionImport.Test("BLA-123");
        test.setStatus(XrayTestExecutionImport.Test.Status.PASS);
        final Calendar calStart = Calendar.getInstance();
        calStart.set(2015, Calendar.DECEMBER, 24, 19, 0, 0);
        test.setStart(calStart.getTime());
        final Calendar calFinish = Calendar.getInstance();
        calFinish.set(2015, Calendar.DECEMBER, 24, 20, 0, 0);
        test.setFinish(calFinish.getTime());

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        /** try if this is sufficient
         objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
         */
        final String result = objectMapper.writeValueAsString(test);
        Assert.assertTrue(result.contains("\"start\":\"2015-12-24T19:00:00+01:00\""));
        Assert.assertTrue(result.contains("\"finish\":\"2015-12-24T20:00:00+01:00\""));
        Assert.assertTrue(result.contains("\"status\":\"PASS\""));
        Assert.assertTrue(result.contains("\"testKey\":\"BLA-123\""));
    }

    @Test
    public void testMarshalTestToJsonWithEvidence() throws JsonProcessingException {

        final XrayTestExecutionImport.Test test = new XrayTestExecutionImport.Test("BLA-123");
        test.setStatus(XrayTestExecutionImport.Test.Status.PASS);
        final Calendar calStart = Calendar.getInstance();
        calStart.set(2015, Calendar.DECEMBER, 24, 19, 0, 0);
        test.setStart(calStart.getTime());
        final Calendar calFinish = Calendar.getInstance();
        calFinish.set(2015, Calendar.DECEMBER, 24, 20, 0, 0);
        test.setFinish(calFinish.getTime());

        final XrayTestExecutionImport.Test.Evidence evidence = new XrayTestExecutionImport.Test.Evidence();
        evidence.setData("YmxhIGJsdWJiDQo=");
        evidence.setFilename("test.txt");
        evidence.setContentType(MediaType.TEXT_PLAIN_TYPE);
        final HashSet<XrayTestExecutionImport.Test.Evidence> evidences = new HashSet<>();
        evidences.add(evidence);
        test.setEvidences(evidences);

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        /** try if this is sufficient
         objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
         */
        final String result = objectMapper.writeValueAsString(test);
        Assert.assertTrue(result.contains("\"start\":\"2015-12-24T19:00:00+01:00\""));
        Assert.assertTrue(result.contains("\"finish\":\"2015-12-24T20:00:00+01:00\""));
        Assert.assertTrue(result.contains("\"status\":\"PASS\""));
        Assert.assertTrue(result.contains("\"testKey\":\"BLA-123\""));
    }

    @Test
    public void testUnmarshalExampleToObject() throws JAXBException, URISyntaxException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final URL url = getClass().getResource("/example.json").toURI().toURL();
        objectMapper.readValue(url, XrayTestExecutionImport.Test[].class);
    }

}
