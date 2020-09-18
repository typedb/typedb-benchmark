package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.neo4j.context.Neo4jContext;
import grakn.simulation.db.neo4j.driver.Transaction;

public abstract class Neo4jAgent<REGION extends Region> extends Agent<REGION, Neo4jContext> {

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
