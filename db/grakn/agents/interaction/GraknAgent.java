package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.grakn.context.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;

public abstract class GraknAgent<REGION extends Region> extends Agent<REGION, GraknContext> {
    private Transaction tx;

    public Transaction tx() {
        return tx;
    }

    @Override
    public void startAction() {
        if (tx == null) {
            tx = backendContext().tx(getSessionKey());
        }
    }

    @Override
    public void stopAction() {
        tx().close();
        tx = null;
    }

    @Override
    public void commitAction() {
        tx().commit();
        tx = null;
    }
}
