package grakn.simulation.db.grakn.driver;

import grakn.client.GraknClient;
import grakn.simulation.common.driver.TransactionalDbOperationFactory;
import org.slf4j.Logger;

import static com.google.common.collect.Iterables.getOnlyElement;

public class GraknOperationFactory extends TransactionalDbOperationFactory<GraknOperation> {

    private final GraknClient.Session session;

    public GraknOperationFactory(GraknClient.Session session, Logger logger) {
        super(logger);
        this.session = session;
    }

    @Override
    public GraknOperation newDbOperation(String tracker, boolean trace) {
        return new GraknOperation(session, logger(), tracker, trace);
    }

}
