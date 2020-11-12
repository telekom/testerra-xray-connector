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

package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testframework.utils.TimerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

@XrayTestSet(key = "SOLI-13191")
public class MixedTestsUnderTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Test
    @XrayTest(key = "SOLI-13139")
    public void fails() {
        TimerUtils.sleep(100);
        logger.info("Test fails");
        Assert.assertTrue(false);
    }

    @Test
    @XrayTest(key = "SOLI-13140")
    public void throwsException() {
        TimerUtils.sleep(100);
        logger.info("Test throws exception");
        throw new IllegalArgumentException();
    }

    @Test(dependsOnMethods = "fails")
    @XrayTest(key = "SOLI-13141")
    public void blocked() {
        logger.info("Test blocked");
    }

    @Test
    public void passesNotAnnotated() {
        TimerUtils.sleep(100);
        logger.info("Test passes");
        Assert.assertTrue(true);
    }

    @Test
    public void failsNotAnnotated() {
        TimerUtils.sleep(100);
        logger.info("Test fails");
        Assert.assertTrue(false);
    }

    @Test
    public void throwsExceptionNotAnnotated() {
        TimerUtils.sleep(100);
        logger.info("Test throws exception");
        throw new IllegalArgumentException();
    }

    @Test(dependsOnMethods = "fails")
    public void blockedNotAnnotated() {
        logger.info("Test blocked");
    }

}
