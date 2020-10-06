package grakn.simulation.db.grakn.driver;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.grakn.action.GraknActionFactory;
import org.slf4j.Logger;

public class GraknDbOperationController extends TransactionDbOperationController<GraknTransaction> {

    public GraknDbOperationController(GraknDriver.GraknSession graknSession, Logger logger) {
        super(graknSession, logger);
    }

    @Override
    public ActionFactory<?, ?> actionFactory() {
        return new GraknActionFactory(this);
    }
}
