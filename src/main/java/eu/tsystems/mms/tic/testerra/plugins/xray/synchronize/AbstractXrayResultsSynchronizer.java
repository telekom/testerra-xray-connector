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

import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.mapper.xray.XrayInfo;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.strategy.SyncStrategy;
import eu.tsystems.mms.tic.testframework.connectors.util.AbstractCommonSynchronizer;
import eu.tsystems.mms.tic.testframework.events.MethodEndEvent;
import eu.tsystems.mms.tic.testframework.info.ReportInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;


public abstract class AbstractXrayResultsSynchronizer extends AbstractCommonSynchronizer implements XrayResultsSynchronizer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final XrayConfig xrayConfig = XrayConfig.getInstance();
    private boolean isSyncInitialized = false;
    private SyncStrategy syncStrategy;

    public void initialize() {

        if (xrayConfig.isSyncEnabled()) {
            try {
                final String project = XrayConfig.getInstance().getProjectKey();
                final XrayTestExecutionInfo executionInfo = getExecutionInfo();
                final String summary = executionInfo.getSummary();
                validateSummary(summary);
                final String description = executionInfo.getDescription();
                validateDescription(description);
                final String revision = executionInfo.getRevision();
                validateRevision(revision);

                final XrayInfo xrayInfo = new XrayInfo(project, summary, description, revision);
                xrayInfo.setUser(executionInfo.getAssignee());
                xrayInfo.setVersion(executionInfo.getFixVersion());
                xrayInfo.setTestEnvironments(executionInfo.getTestEnvironments());

                syncStrategy = xrayConfig.getSyncStrategyClass()
                        .getDeclaredConstructor(new Class<?>[] {XrayInfo.class, XrayMapper.class, XrayTestExecutionUpdates.class})
                        .newInstance(xrayInfo, getXrayMapper(), getExecutionUpdates());
                syncStrategy.onStart();
                isSyncInitialized = true;
                logger.info("intialization successful");
            } catch (final Exception e) {
                disableSyncWithWarning(e);
            }
        }
    }

    public void shutdown() {
        if (isSyncInitialized) {
            try {
                syncStrategy.onFinish();
            } catch (final Exception e) {
                disableSyncWithWarning(e);
            }
        }
    }

    @Override
    protected void pOnTestSuccess(MethodEndEvent event) {
        if (isSyncInitialized) {
            syncStrategy.onTestSuccess(event);
            event.getMethodContext().addPriorityMessage("Synchronization to X-ray successful.");
        }
    }

    @Override
    protected void pOnTestFailure(MethodEndEvent event) {
        if (isSyncInitialized) {
            syncStrategy.onTestFailure(event);
            event.getMethodContext().addPriorityMessage("Synchronization to X-ray successful.");
        }
    }

    @Override
    protected void pOnTestSkip(MethodEndEvent event) {
        if (isSyncInitialized) {
            syncStrategy.onTestSkip(event);
            event.getMethodContext().addPriorityMessage("Synchronization to X-ray successful.");
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

    private void validateSummary(final String summary) throws NotSyncableException {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!summary.matches(xrayConfig.getValidationRegexSummary())) {
            throw new NotSyncableException(String.format("summary %s does not conform regex %s",
                    summary, xrayConfig.getValidationRegexSummary()));
        }
    }

    private void validateRevision(final String revision) throws NotSyncableException {
        final XrayConfig xrayConfig = XrayConfig.getInstance();
        if (!revision.matches(xrayConfig.getValidationRegexRevision())) {
            throw new NotSyncableException(String.format("revision %s does not conform regex %s",
                    revision, xrayConfig.getValidationRegexRevision()));
        }
    }

    private void validateDescription(final String description) throws NotSyncableException {
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

    private void disableSyncWithWarning(Exception e) {
        isSyncInitialized = false;
        final String message = "An unexpected exception occurred. Syncing is aborted.";
        ReportInfo.getDashboardInfo().addInfo(1, message + e.getMessage());
        logger.error(message, e);
    }
}
