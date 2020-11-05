/*
 * Testerra Xray-Connector
 *
 * (C) 2020, Eric Kubenka, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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

package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.synchronizer;

import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.AbstractXrayResultsSynchronizer;

/**
 * This is a valid Xray synchronizer for multiple test executions
 * How it works?
 * Imagine you run parallel tests with different identifier per thread, e.g you run your tests parallel,
 * but with a simple property you define on which device your test should run.
 * This property is probably thread related, because you mainly run the test for device A in one thread and for device B in another thread.
 * With this approach we will override the basic methods of {@link AbstractXrayResultsSynchronizer} because we dont want to initialize a static synchronizer.
 * We want to initialize a synchronizer for each thread.
 * Therefore we use some thread locals... everything else is pure magic :)
 * <p>
 * TODO - For testing purpose only, we have to deactivate this class, because tests will fail otherwise.
 */
public class MultipleExecutionSimulatedTestRunXrayResultsSynchronizer {
    //public class MultipleExecutionSimulatedTestRunXrayResultsSynchronizer extends AbstractXrayResultsSynchronizer {
    //
    //    private static final ThreadLocal<Boolean> isSyncInitialized = new ThreadLocal<>();
    //    private static final ThreadLocal<SyncStrategy> syncStrategy = new ThreadLocal<>();
    //
    //    @Override
    //    public XrayTestExecutionInfo getExecutionInfo() {
    //
    //        return new XrayTestExecutionInfo() {
    //            @Override
    //            public String getSummary() {
    //                return "Simulated Test Run - " + Thread.currentThread().getName();
    //            }
    //
    //            @Override
    //            public String getDescription() {
    //                return null;
    //            }
    //
    //            @Override
    //            public String getRevision() {
    //                return null;
    //            }
    //
    //            @Override
    //            public String getAssignee() {
    //                return null;
    //            }
    //
    //            @Override
    //            public String getFixVersion() {
    //                return null;
    //            }
    //
    //            @Override
    //            public List<String> getTestEnvironments() {
    //                return null;
    //            }
    //        };
    //    }
    //
    //    @Override
    //    public XrayTestExecutionUpdates getExecutionUpdates() {
    //        return new DefaultTestExecutionUpdates();
    //    }
    //
    //    public XrayMapper getXrayMapper() {
    //        return new ResultMapper();
    //    }
    //
    //    @Override
    //    public void initialize() {
    //        // Do nothing because we want to have a thread-safe way of sync...
    //    }
    //
    //    @Override
    //    protected void pOnTestSuccess(MethodEndEvent event) {
    //        pInitialize();
    //        if (isSyncInitialized.get() != null && isSyncInitialized.get()) {
    //            syncStrategy.get().onTestSuccess(event);
    //            event.getMethodContext().addPriorityMessage("Synchronization to X-ray successful.");
    //        }
    //    }
    //
    //    @Override
    //    protected void pOnTestFailure(MethodEndEvent event) {
    //        pInitialize();
    //        if (isSyncInitialized.get() != null && isSyncInitialized.get()) {
    //            syncStrategy.get().onTestFailure(event);
    //            event.getMethodContext().addPriorityMessage("Synchronization to X-ray successful.");
    //        }
    //    }
    //
    //    @Override
    //    protected void pOnTestSkip(MethodEndEvent event) {
    //        pInitialize();
    //        if (isSyncInitialized.get() != null && isSyncInitialized.get()) {
    //            syncStrategy.get().onTestSkip(event);
    //            event.getMethodContext().addPriorityMessage("Synchronization to X-ray successful.");
    //        }
    //    }
    //
    //    private void pInitialize() {
    //
    //        if (isSyncInitialized.get() == null) {
    //            if (xrayConfig.isSyncEnabled()) {
    //                try {
    //                    final String project = XrayConfig.getInstance().getProjectKey();
    //                    final XrayTestExecutionInfo executionInfo = getExecutionInfo();
    //                    final String summary = executionInfo.getSummary();
    //                    validateSummary(summary);
    //                    final String description = executionInfo.getDescription();
    //                    validateDescription(description);
    //                    final String revision = executionInfo.getRevision();
    //                    validateRevision(revision);
    //
    //                    final XrayInfo xrayInfo = new XrayInfo(project, summary, description, revision);
    //                    xrayInfo.setUser(executionInfo.getAssignee());
    //                    xrayInfo.setVersion(executionInfo.getFixVersion());
    //                    xrayInfo.setTestEnvironments(executionInfo.getTestEnvironments());
    //
    //                    syncStrategy.set(xrayConfig.getSyncStrategyClass()
    //                            .getDeclaredConstructor(new Class<?>[] {XrayInfo.class, XrayMapper.class, XrayTestExecutionUpdates.class})
    //                            .newInstance(xrayInfo, getXrayMapper(), getExecutionUpdates()));
    //                    syncStrategy.get().onStart();
    //                    isSyncInitialized.set(true);
    //                    log().info("intialization successful");
    //                } catch (final Exception e) {
    //                    isSyncInitialized.set(false);
    //                    final String message = "An unexpected exception occurred. Syncing is aborted.";
    //                    ReportInfo.getDashboardInfo().addInfo(1, message + e.getMessage());
    //                    log().error(message, e);
    //                }
    //            }
    //        }
    //    }
}
