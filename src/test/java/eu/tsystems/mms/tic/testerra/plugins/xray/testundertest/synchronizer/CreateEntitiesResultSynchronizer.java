package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.synchronizer;

import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.AbstractXrayResultsSynchronizer;
import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayMapper;
import eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.mapper.CreateEntitiesMapper;

/**
 * Created on 17.03.2022
 *
 * @author mgn
 */
public class CreateEntitiesResultSynchronizer extends AbstractXrayResultsSynchronizer {

    public XrayMapper getXrayMapper() {
        return new CreateEntitiesMapper();
    }

}
