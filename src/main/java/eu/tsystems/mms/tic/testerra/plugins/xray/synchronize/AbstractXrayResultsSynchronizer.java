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

package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.strategy.AbstractSyncStrategy;
import eu.tsystems.mms.tic.testframework.connectors.util.AbstractCommonSynchronizer;
import eu.tsystems.mms.tic.testframework.events.MethodEndEvent;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


public abstract class AbstractXrayResultsSynchronizer extends AbstractCommonSynchronizer implements XrayResultsSynchronizer, Loggable {

    protected final XrayConfig xrayConfig = XrayConfig.getInstance();
    private boolean isSyncInitialized = false;
    private AbstractSyncStrategy syncStrategy;

    public void initialize() {

        if (xrayConfig.isSyncEnabled()) {
            try {
                final String project = XrayConfig.getInstance().getProjectKey();
                final XrayTestExecutionInfo executionInfo = getExecutionInfo();
                if (executionInfo == null) {
                    throw new RuntimeException("No " + XrayTestExecutionInfo.class.getSimpleName() + " provided");
                }
                final Optional<String> summary = Optional.ofNullable(executionInfo.getSummary());
                summary.ifPresent(this::validateSummary);
                final Optional<String> description = Optional.ofNullable(executionInfo.getDescription());
                description.ifPresent(this::validateDescription);
                final Optional<String> revision = Optional.ofNullable(executionInfo.getRevision());
                revision.ifPresent(this::validateRevision);

                final XrayInfo xrayInfo = new XrayInfo(project, summary.orElse(""), description.orElse(""), revision.orElse(""));
                xrayInfo.setUser(executionInfo.getAssignee());
                xrayInfo.setVersion(executionInfo.getFixVersion());
                xrayInfo.setTestEnvironments(executionInfo.getTestEnvironments());

                Optional<XrayMapper> xrayMapper = Optional.ofNullable(getXrayMapper());
                Optional<XrayTestExecutionUpdates> executionUpdates = Optional.ofNullable(getExecutionUpdates());

                syncStrategy = xrayConfig.getSyncStrategyClass()
                        .getDeclaredConstructor(new Class<?>[]{XrayInfo.class, XrayMapper.class, XrayTestExecutionUpdates.class})
                        .newInstance(xrayInfo, xrayMapper.orElse(new EmptyMapper()), executionUpdates.orElse(new EmptyTestExecutionUpdates()));
                syncStrategy.onStart();
                isSyncInitialized = true;
            } catch (UniformInterfaceException e) {
                try {
                    handleException(e);
                } catch (Exception other) {
                    disableSyncWithWarning(e);
                }
            } catch (final Exception e) {
                disableSyncWithWarning(e);
            }
        }
    }

    public void shutdown() {
        if (isSyncInitialized) {
            try {
                syncStrategy.onFinish();
            } catch (UniformInterfaceException e) {
                try {
                    handleException(e);
                } catch (Exception other) {
                    disableSyncWithWarning(e);
                }
            } catch (final Exception e) {
                disableSyncWithWarning(e);
            }
        }
    }

    @Override
    protected void pOnTestSuccess(MethodEndEvent event) {
        if (isSyncInitialized) {
            syncStrategy.onTestSuccess(event);
            event.getMethodContext().addPriorityMessage("Synchronization to Xray successful.");
        }
    }

    @Override
    protected void pOnTestFailure(MethodEndEvent event) {
        if (isSyncInitialized) {
            syncStrategy.onTestFailure(event);
            event.getMethodContext().addPriorityMessage("Synchronization to Xray successful.");
        }
    }

    @Override
    protected void pOnTestSkip(MethodEndEvent event) {
        if (isSyncInitialized) {
            syncStrategy.onTestSkip(event);
            event.getMethodContext().addPriorityMessage("Synchronization to Xray successful.");
        }
    }

    @Override
    public XrayMapper getXrayMapper() {
        /* no mapping */
        return new EmptyMapper();
    }

    @Override
    public XrayTestExecutionUpdates getExecutionUpdates() {
        /* no updates */
        return new EmptyTestExecutionUpdates();
    }

    protected void validateSummary(final String summary) throws NotSyncableException {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!summary.matches(xrayConfig.getValidationRegexSummary())) {
            throw new NotSyncableException(String.format("summary %s does not conform regex %s",
                    summary, xrayConfig.getValidationRegexSummary()));
        }
    }

    protected void validateRevision(final String revision) throws NotSyncableException {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!revision.matches(xrayConfig.getValidationRegexRevision())) {
            throw new NotSyncableException(String.format("revision %s does not conform regex %s",
                    revision, xrayConfig.getValidationRegexRevision()));
        }
    }

    protected void validateDescription(final String description) throws NotSyncableException {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!description.matches(xrayConfig.getValidationRegexDescription())) {
            throw new NotSyncableException(String.format("description %s does not conform regex %s",
                    description, xrayConfig.getValidationRegexDescription()));
        }
    }

    protected void addTestExecutionAttachment(final InputStream is, final String fileName) {
        syncStrategy.addTestExecutionAttachment(is, fileName);
    }

    protected void addTestExecutionComment(final String comment) {
        syncStrategy.addTestExecutionComment(comment);
    }

    private void reportError(String message, Exception e) {
        //ReportInfo.getDashboardInfo().addInfo(1, message + e.getMessage());
        log().error(message, e);
    }

    private void disableSyncWithWarning(Exception e) {
        isSyncInitialized = false;
        reportError("An unexpected exception occurred. Syncing is aborted.", e);
    }

    private void handleException(UniformInterfaceException e) throws IOException {
        ClientResponse response = e.getResponse();
        Map<String, Object> responseMap = new ObjectMapper().readValue(response.getEntityInputStream(), Map.class);
        isSyncInitialized = false;
        reportError(formatErrorMessages(responseMap).toString(), e);
    }

    private StringBuilder formatErrorMessages(Map<String, Object> responseMap) throws ClassCastException {
        StringBuilder sb = new StringBuilder();

        ArrayList<String> errorMessages = null;
        if (responseMap.containsKey("errorMessages")) {
            errorMessages = (ArrayList<String>) responseMap.get("errorMessages");
            sb.append(String.join("\n", errorMessages));
        }

        Object errors = responseMap.get("errors");
        if (errors != null) {
            if (errorMessages != null && errorMessages.size() > 0) {
                sb.append(": \n");
            }

            Map<String, Object> errorsMap = (Map<String, Object>)errors;
            errorsMap.forEach((s, o) -> {
                sb.append(s).append(": ").append(o);
            });
        }

        return sb;
    }
}
