package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.base.TransactionalAgent;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.grakn.context.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;

public abstract class GraknAgent<REGION extends Region> extends TransactionalAgent<REGION, GraknContext, Transaction, ConceptMap> {

}
