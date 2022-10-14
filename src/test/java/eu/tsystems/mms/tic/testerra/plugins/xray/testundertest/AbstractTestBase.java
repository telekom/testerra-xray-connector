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

import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation.Provider;
import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import eu.tsystems.mms.tic.testframework.utils.TimerUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.annotation.Provider.DataDrivenType;

public abstract class AbstractTestBase extends TesterraTest {

    private static final int SLEEP_TIME = 100;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testMapperPasses() {
        logger.info("starting test 'passes'");
        TimerUtils.sleep(SLEEP_TIME);
        logger.info("test 'passes' done");
        Assert.assertTrue(true);
    }

    @Test
    public void testMapperFails() {
        logger.info("starting test 'fails'");
        TimerUtils.sleep(SLEEP_TIME);
        logger.info("test 'fails' done");
        Assert.assertTrue(false);
    }

    @Test(dependsOnMethods = "testMapperFails")
    public void testMapperSkips() {
        logger.info("this shouldn't be read anytime");
    }

    @Test(dataProviderClass = Provider.class, dataProvider = "provide")
    public void parametrized(DataDrivenType type, int number) {
        logger.info("starting test 'dataDriven' with parameters {}", String.format("type: %s, number: %s", type, number));
        TimerUtils.sleep(SLEEP_TIME);
        switch (type) {
            case Foo:
                logger.info("test 'parametrized' done");
                Assert.assertTrue(true);
                break;
            case Bar:
                logger.info("test 'parametrized' done");
                Assert.fail();
                break;
            default:
                throw new NotImplementedException("unknown type");
        }
    }
}
