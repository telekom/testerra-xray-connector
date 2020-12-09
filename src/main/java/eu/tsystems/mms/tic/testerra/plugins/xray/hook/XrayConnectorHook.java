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

import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.AbstractXrayResultsSynchronizer;
import eu.tsystems.mms.tic.testframework.hooks.ModuleHook;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * XrayConnectorHook
 * <p>
 * Date: 21.08.2019
 * Time: 12:17
 *
 * @author Eric Kubenka
 */
public class XrayConnectorHook implements ModuleHook, Loggable {

    private static final List<AbstractXrayResultsSynchronizer> XRAY_LISTENER = new LinkedList<>();

    @Override
    public void init() {

        this.initListener();

    }

    @Override
    public void terminate() {

        for (final AbstractXrayResultsSynchronizer xraySynchronizer : XRAY_LISTENER) {
            xraySynchronizer.shutdown();
            TesterraListener.getEventBus().unregister(xraySynchronizer);
        }
    }

    private void initListener() {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.addClassLoader(Thread.currentThread().getContextClassLoader());
        configurationBuilder.setUrls(ClasspathHelper.forJavaClassPath());
        final Reflections reflections = new Reflections(configurationBuilder);
        final Set<Class<? extends AbstractXrayResultsSynchronizer>> hooks = reflections.getSubTypesOf(AbstractXrayResultsSynchronizer.class);

        if (hooks.isEmpty()) {
            log().debug("No Xray result listener found");
        }

        hooks.forEach(aClass -> {
            try {
                final AbstractXrayResultsSynchronizer xrayListener = aClass.getConstructor().newInstance();
                log().debug("Calling xray result listener " + aClass.getSimpleName() + "...");
                xrayListener.initialize();
                TesterraListener.getEventBus().register(xrayListener);
                XRAY_LISTENER.add(xrayListener);
            } catch (Exception e) {
                log().error("Could not load Xray result listener", e);
            }
        });
    }
}
