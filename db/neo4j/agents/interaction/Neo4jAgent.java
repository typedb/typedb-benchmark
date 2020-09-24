package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.base.TransactionalAgent;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.neo4j.context.Neo4jContext;
import grakn.simulation.db.neo4j.driver.Transaction;
import org.neo4j.driver.Record;

public abstract class Neo4jAgent<REGION extends Region> extends TransactionalAgent<REGION, Neo4jContext, Transaction, Record> {

}
