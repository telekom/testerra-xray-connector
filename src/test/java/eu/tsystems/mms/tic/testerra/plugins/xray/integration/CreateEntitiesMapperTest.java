package eu.tsystems.mms.tic.testerra.plugins.xray.integration;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTestSet;
import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testerra.plugins.xray.hook.XrayConnectorHook;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.synchronizer.CreateEntitiesResultSynchronizer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created on 17.03.2022
 *
 * @author mgn
 */
@XrayTestSet
public class CreateEntitiesMapperTest {

    @BeforeClass
    public void prepareWebResource() {
        XrayConfig.init("sync.test.properties");
        Assert.assertTrue(XrayConfig.getInstance().isSyncEnabled());
        XrayConnectorHook.getInstance().setXrayResultsSynchronizer(new CreateEntitiesResultSynchronizer());
    }

    @Test
    public void testNewXrayTest01() {

    }

}
