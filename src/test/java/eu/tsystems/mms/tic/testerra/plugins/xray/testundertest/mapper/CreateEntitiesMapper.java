package eu.tsystems.mms.tic.testerra.plugins.xray.testundertest.mapper;

import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.DefaultSummaryMapper;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;

/**
 * Created on 17.03.2022
 *
 * @author mgn
 */
public class CreateEntitiesMapper extends DefaultSummaryMapper {

    @Override
    public boolean shouldCreateNewTestSet(ClassContext classContext) {
        return true;
    }

    @Override
    public boolean shouldCreateNewTest(MethodContext methodContext) {
        return true;
    }

}
