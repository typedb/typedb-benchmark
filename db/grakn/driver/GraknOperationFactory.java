package grakn.simulation.db.grakn.driver;

import grakn.simulation.db.common.driver.TransactionalDbOperationFactory;
import org.slf4j.Logger;

import static com.google.common.collect.Iterables.getOnlyElement;

public class GraknOperationFactory extends TransactionalDbOperationFactory<GraknOperation> {

    public GraknOperationFactory(GraknClient.Session session, Logger logger) {
        super(logger);
    }

    @Override
    public GraknOperation newDbOperation(String tracker) {
        return new GraknOperation(session, logger(), tracker);
    }

}
