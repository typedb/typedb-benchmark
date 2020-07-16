package grakn.simulation.db.neo4j.driver;

import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.neo4j.agents.interaction.Neo4jQuery;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;

import static org.neo4j.driver.Values.parameters;

public class Neo4jDriverWrapper implements DriverWrapper {

    private Driver driver = null;

    public Driver getClient() {
        return driver;
    }

    @Override
    public Neo4jDriverWrapper open(String uri) {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( "neo4j", "admin" ) );;
        return this;
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public Session session(String database) {
        return new Session(driver.session());
    }

    public static class Session extends DriverWrapper.Session {

        private org.neo4j.driver.Session session;

        Session(org.neo4j.driver.Session session) {
            this.session = session;
        }

        @Override
        public void close() {
            this.session.close();
        }

        @Override
        public Transaction transaction() {
            return new Transaction(session.beginTransaction());
        }

        public class Transaction extends DriverWrapper.Session.Transaction {

            private org.neo4j.driver.Transaction transaction;

            Transaction(org.neo4j.driver.Transaction transaction) {
                this.transaction = transaction;
            }

            @Override
            public void close() {
                transaction.close();
            }

            @Override
            public void commit() {
                transaction.commit();
            }

            @Override
            public org.neo4j.driver.Transaction forNeo4j() {
                return transaction;
            }
        }
    }

    public static Result run(org.neo4j.driver.Transaction transaction, Neo4jQuery query) {
        return transaction.run(query.template(), parameters(query.parameters()));
    }
}
