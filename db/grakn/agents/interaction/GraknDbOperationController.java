package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.action.ActionFactory;
import grakn.simulation.db.common.agents.base.TransactionDbOperationController;
import grakn.simulation.db.grakn.agents.action.GraknActionFactory;
import grakn.simulation.db.grakn.context.GraknDriver;
import grakn.simulation.db.grakn.driver.GraknTransaction;
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
