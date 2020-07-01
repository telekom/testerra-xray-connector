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

package eu.tsystems.mms.tic.testerra.plugins.xray.config;

import com.google.common.collect.ImmutableList;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.strategy.AdHocSyncStrategy;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.strategy.PostHocSyncStrategy;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.strategy.SyncStrategy;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.exceptions.TesterraSetupException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XrayConfig {

    protected static final Logger logger = LoggerFactory.getLogger(XrayConfig.class);
    private static String propFileName = "xray.properties";
    private static XrayConfig instance;
    private final String previousResultsFilename;
    private final String projectKey;
    private final String username;
    private final Class<? extends SyncStrategy> syncStrategyClass;
    private final String validationRegexDescription;
    private final String validationRegexRevision;
    private final String validationRegexSummary;
    private final URI restServiceUri;
    private final List<String> transitionsOnDone;
    private final List<String> transitionsOnCreated;
    private final List<String> transitionsOnUpdated;
    private final String password;
    private final boolean webResourceFilterLoggingEnabled;
    private final boolean webResourceFilterGetRequestsOnlyEnabled;
    private final boolean isSyncSkippedTests;
    private final String testExecutionStartTimeFieldName;
    private final String testExecutionStartTimeJQLTerm;
    private final String testExecutionFinishTimeFieldName;
    private final String testExecutionFinishTimeJQLTerm;
    private final String revisionFieldName;
    private final String revisionJQLTerm;
    private final String testEnvironmentsFieldName;
    private final String testEnvironmentsJQLTerm;

    private boolean isSyncEnabled;
    private String fakeTestExecutionKey;

    private XrayConfig() {
        PropertyManager.loadProperties(propFileName);

        projectKey = PropertyManager.getProperty("xray.project.key");
        username = PropertyManager.getProperty("xray.user");
        password = PropertyManager.getProperty("xray.password");

        isSyncEnabled = PropertyManager.getBooleanProperty("xray.sync.enabled", false);
        isSyncSkippedTests = PropertyManager.getBooleanProperty("xray.sync.skipped", false);

        URI uri = null;
        try {
            uri = new URI(PropertyManager.getProperty("xray.rest.service.uri"));
        } catch (final URISyntaxException e) {
            logger.error(e.getMessage());
        }
        restServiceUri = uri;

        switch (PropertyManager.getProperty("xray.sync.strategy", "adhoc")) {
            case "posthoc":
                syncStrategyClass = PostHocSyncStrategy.class;
                break;
            case "adhoc":
                syncStrategyClass = AdHocSyncStrategy.class;
                break;
            default:
                throw new TesterraSetupException("unknown strategy given");
        }
        webResourceFilterLoggingEnabled = PropertyManager.getBooleanProperty("xray.webresource.filter.logging.enabled", false);
        webResourceFilterGetRequestsOnlyEnabled = PropertyManager.getBooleanProperty("xray.webresource.filter.getrequestsonly.enabled", false);
        fakeTestExecutionKey = PropertyManager.getProperty("xray.webresource.filter.getrequestsonly.fake.response.key", "FAKE-666666");

        previousResultsFilename = PropertyManager.getProperty("xray.previous.result.filename", "");

        final int testExecutionStartTimeId = PropertyManager.getIntProperty("xray.test.execution.start.time.field.id", 14270);
        testExecutionStartTimeFieldName = toFieldName(testExecutionStartTimeId);
        testExecutionStartTimeJQLTerm = toJQLTerm(testExecutionStartTimeId);
        final int testExecutionFinishTimeId = PropertyManager.getIntProperty("xray.test.execution.finish.time.field.id", 14271);
        testExecutionFinishTimeFieldName = toFieldName(testExecutionFinishTimeId);
        testExecutionFinishTimeJQLTerm = toJQLTerm(testExecutionFinishTimeId);

        final int revisionId = PropertyManager.getIntProperty("xray.test.execution.revision.field.id", 14272);
        revisionFieldName = toFieldName(revisionId);
        revisionJQLTerm = toJQLTerm(revisionId);

        final int testEnvironmentsId = PropertyManager.getIntProperty("xray.test.execution.test-environments.field.id", 15155);
        testEnvironmentsFieldName = toFieldName(testEnvironmentsId);
        testEnvironmentsJQLTerm = toJQLTerm(testEnvironmentsId);

        validationRegexDescription = PropertyManager.getProperty("xray.validation.description.regexp", ".*");
        validationRegexRevision = PropertyManager.getProperty("xray.validation.revision.regexp", ".*");
        validationRegexSummary = PropertyManager.getProperty("xray.validation.summary.regexp", ".*");

        transitionsOnCreated = createTransitionList(PropertyManager.getProperty("xray.transitions.on.created"));
        transitionsOnUpdated = createTransitionList(PropertyManager.getProperty("xray.transitions.on.updated"));
        transitionsOnDone = createTransitionList(PropertyManager.getProperty("xray.transitions.on.done"));

    }

    public static synchronized void init(String propFileName) {
        logger.info("reading configuration from file: {}", propFileName);
        if (instance != null) {
            throw new IllegalStateException("property file was already read, init before first call to getInstance()");
        }
        XrayConfig.propFileName = propFileName;
    }

    public static synchronized void reset() {
        instance = null;
    }

    public static synchronized XrayConfig getInstance() {
        if (instance == null) {
            instance = new XrayConfig();
        }
        return instance;
    }

    private String toJQLTerm(int fieldId) {
        return String.format("cf[%s]", fieldId);
    }

    private String toFieldName(int fieldId) {
        return String.format("customfield_%d", fieldId);
    }

    public String getTestExecutionStartTimeFieldName() {
        return testExecutionStartTimeFieldName;
    }

    public String getTestExecutionStartTimeJQLTerm() {
        return testExecutionStartTimeJQLTerm;
    }

    public String getTestExecutionFinishTimeFieldName() {
        return testExecutionFinishTimeFieldName;
    }

    public String getTestExecutionFinishTimeJQLTerm() {
        return testExecutionFinishTimeJQLTerm;
    }

    public String getRevisionFieldName() {
        return revisionFieldName;
    }

    public String getRevisionJQLTerm() {
        return revisionJQLTerm;
    }

    public boolean isSyncEnabled() {
        return isSyncEnabled;
    }

    private List<String> createTransitionList(String prop) {
        if (!prop.equals("")) {
            String COMMA_SEPARATED_REGEXP = "\\s*,\\s*";
            return ImmutableList.copyOf(prop.split(COMMA_SEPARATED_REGEXP));
        } else {
            return ImmutableList.copyOf(new ArrayList<String>());
        }
    }

    public Class<? extends SyncStrategy> getSyncStrategyClass() {
        return syncStrategyClass;
    }

    public String getPreviousResultsFilename() {
        return previousResultsFilename;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getUsername() {
        return username;
    }

    public String getValidationRegexDescription() {
        return validationRegexDescription;
    }

    public String getValidationRegexRevision() {
        return validationRegexRevision;
    }

    public String getValidationRegexSummary() {
        return validationRegexSummary;
    }

    public URI getRestServiceUri() {
        return restServiceUri;
    }

    public List<String> getTransitionsOnDone() {
        return transitionsOnDone;
    }

    public List<String> getTransitionsOnCreated() {
        return transitionsOnCreated;
    }

    public List<String> getTransitionsOnUpdated() {
        return transitionsOnUpdated;
    }

    public String getPassword() {
        return password;
    }

    public boolean isWebResourceFilterLoggingEnabled() {
        return webResourceFilterLoggingEnabled;
    }

    public boolean isWebResourceFilterGetRequestsOnlyEnabled() {
        return webResourceFilterGetRequestsOnlyEnabled;
    }

    public String getFakeTestExecutionKey() {
        return fakeTestExecutionKey;
    }

    public int getNumOfSyncThreads() {
        /** as negotiated with Andre Lehman on 2016-04-28 */
        return 16;
    }

    public boolean isSyncSkippedTests() {
        return isSyncSkippedTests;
    }

    public String getTestEnvironmentsFieldName() {
        return testEnvironmentsFieldName;
    }

    public String getTestEnvironmentsJQLTerm() {
        return testEnvironmentsJQLTerm;
    }
}
