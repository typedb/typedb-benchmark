package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.driver.TransactionalDbOperationFactory;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.neo4j.driver.Neo4jDriver;
import grakn.simulation.db.neo4j.driver.Neo4jTransaction;
import org.neo4j.driver.Record;

public abstract class Neo4jAgent<REGION extends Region> extends TransactionalDbOperationFactory<REGION, Neo4jDriver, Neo4jTransaction, Record> {

}
