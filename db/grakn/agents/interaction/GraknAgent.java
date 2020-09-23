package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.utils.CheckMethod;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.grakn.context.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;

public abstract class GraknAgent<REGION extends Region> extends Agent<REGION, GraknContext> {
    private Transaction tx;
    private String action;

    public Transaction tx() {
        return tx;
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public void newAction(String action) {
        CheckMethod.checkMethodExists(this, action);
        this.action = action;
        if (tx == null) {
            tx = backendContext().tx(getSessionKey(), logger(), tracker() + ":" + this.action);
        }
    }

    @Override
    public void closeAction() {
        tx().close();
        tx = null;
    }

    @Override
    public void commitAction() {
        tx().commit();
        tx = null;
    }
}
