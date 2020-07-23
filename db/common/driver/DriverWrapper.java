package grakn.simulation.db.common.driver;

import grakn.client.GraknClient;
import org.neo4j.driver.async.AsyncTransaction;

import java.util.concurrent.CompletionStage;

public interface DriverWrapper extends AutoCloseable {

    DriverWrapper open(String uri);
    void close();
    Session session(String database);

    abstract class Session {
        public abstract void close();
        public abstract Transaction transaction();

        public abstract class Transaction implements AutoCloseable {
            public abstract void close();
            public abstract void commit();
            public GraknClient.Transaction forGrakn() {
                throw new ClassCastException("Can't cast transaction into a Grakn transaction");
            }
            public CompletionStage<AsyncTransaction> forNeo4j() {
                throw new ClassCastException("Can't cast transaction into a Neo4j transaction");
            }
        }
    }

}
