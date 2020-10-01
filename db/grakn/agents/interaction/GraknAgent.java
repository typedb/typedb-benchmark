package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.action.ActionFactory;
import grakn.simulation.db.common.agents.base.TransactionalAgent;
import grakn.simulation.db.grakn.agents.action.GraknActionFactory;
import grakn.simulation.db.grakn.context.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;

public abstract class GraknAgent extends TransactionalAgent<GraknContext, Transaction> {

    public ActionFactory<GraknAgent, ConceptMap> actionFactory() {
        return new GraknActionFactory(this);
    }
}
