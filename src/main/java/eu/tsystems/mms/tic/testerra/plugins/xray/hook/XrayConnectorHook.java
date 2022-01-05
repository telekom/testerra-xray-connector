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

package eu.tsystems.mms.tic.testerra.plugins.xray.hook;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestAnnotationConverter;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.AbstractXrayResultsSynchronizer;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayResultsSynchronizer;
import eu.tsystems.mms.tic.testframework.hooks.ModuleHook;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.DefaultReport;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import java.util.Objects;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * XrayConnectorHook
 * <p>
 * Date: 21.08.2019
 * Time: 12:17
 *
 * @author Eric Kubenka
 */
public class XrayConnectorHook implements ModuleHook, Loggable, ISuiteListener {
    private XrayResultsSynchronizer xrayResultsSynchronizer;
    private static XrayConnectorHook instance;

    public XrayConnectorHook() {
        instance = this;
    }

    @Override
    @Subscribe
    public void onStart(ISuite suite) {
        this.initDefaultListener();

        EventBus eventBus = TesterraListener.getEventBus();
        eventBus.unregister(this);
    }

    public static XrayConnectorHook getInstance() {
        return instance;
    }

    public synchronized void setXrayResultsSynchronizer(XrayResultsSynchronizer xrayResultsSynchronizer) {
        if (this.xrayResultsSynchronizer != null) {
            this.xrayResultsSynchronizer.shutdown();
        }
        log().info(String.format("Registering %s", xrayResultsSynchronizer.getClass().getSimpleName()));
        this.xrayResultsSynchronizer = xrayResultsSynchronizer;
        this.xrayResultsSynchronizer.initialize();
    }

    @Override
    public void init() {
        EventBus eventBus = TesterraListener.getEventBus();
        eventBus.register(this);

        DefaultReport report = (DefaultReport) TesterraListener.getReport();
        report.registerAnnotationConverter(XrayTest.class, new XrayTestAnnotationConverter());
    }

    @Override
    public void terminate() {
        DefaultReport report = (DefaultReport) TesterraListener.getReport();
        report.unregisterAnnotationConverter(XrayTest.class);

        if (this.xrayResultsSynchronizer != null) {
            this.xrayResultsSynchronizer.shutdown();
        }
    }

    private synchronized void initDefaultListener() {
        if (this.xrayResultsSynchronizer != null) {
            return;
        }

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.addClassLoader(Thread.currentThread().getContextClassLoader());
        configurationBuilder.setUrls(ClasspathHelper.forJavaClassPath());
        final Reflections reflections = new Reflections(configurationBuilder);
        final Set<Class<? extends XrayResultsSynchronizer>> synchronizers = reflections.getSubTypesOf(XrayResultsSynchronizer.class);

        if (synchronizers.isEmpty()) {
            log().warn(String.format("No implementations of %s found", XrayResultsSynchronizer.class.getSimpleName()));
        }

        synchronizers.stream()
                .filter(aClass -> aClass != AbstractXrayResultsSynchronizer.class)
                .map(aClass -> {
                    try {
                        return aClass.getConstructor().newInstance();
                    } catch (Exception e) {
                        log().error(String.format("Could not create instance of %s", aClass.getSimpleName()), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(this::setXrayResultsSynchronizer);
    }
}
