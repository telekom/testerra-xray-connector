package eu.tsystems.mms.tic.testerra.plugins.xray;

import eu.tsystems.mms.tic.testerra.plugins.xray.annotation.XrayTest;
import eu.tsystems.mms.tic.testerra.plugins.xray.tests.AbstractTest;
import eu.tsystems.mms.tic.testframework.annotations.Fails;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import org.testng.annotations.Test;

public class ExportTest extends AbstractTest {

    @Test(description = "This is a simple test for exported annotations, see more here: https://docs.testerra.io")
    @XrayTest(key = {"SWFTE-802", "SWFTE-989"})
    @Fails(description = "This should fail", ticketString = "https://testerra.io")
    public void test_exportAnnotation() {
        PropertyManager.getThreadLocalProperties().setProperty("xray.sync.enabled", "false");
    }
}
