package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.neo4j.context.Neo4jContext;
import grakn.simulation.db.neo4j.driver.Transaction;

public abstract class Neo4jAgent<REGION extends Region> extends Agent<REGION, Neo4jContext> {

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
        registerMethodTrace(action);
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
