package grakn.simulation.db.neo4j.driver;

import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.neo4j.agents.interaction.Neo4jQuery;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.exceptions.TransientException;

import java.util.ArrayList;
import java.util.List;

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
            return new Transaction(session);
        }

        public class Transaction extends DriverWrapper.Session.Transaction {

            private org.neo4j.driver.Transaction transaction;
            private List<Neo4jQuery> queries = new ArrayList<>();
            private org.neo4j.driver.Session session;

            Transaction(org.neo4j.driver.Session session) {
                this.session = session;
                this.transaction = newTransaction();
            }

            private org.neo4j.driver.Transaction newTransaction() {
                return session.beginTransaction();
            }

            @Override
            public void close() {
                transaction.close();
            }

            @Override
            public void commit() {
                Throwable txEx = null;
                int RETRIES = 5;
                int BACKOFF = 100;
                for (int i = 0; i < RETRIES; i++) {
                    try {
                        transaction.commit();
                        return;
                    } catch (Throwable ex) {
                        txEx = ex;

                        // Add whatever exceptions to retry on here
                        if (!(ex instanceof TransientException)) {
                            throw ex;
                        }
                    }

                    // Wait so that we don't immediately get into the same deadlock
                    if (i < RETRIES - 1) {
                        try {
                            transaction = newTransaction();
                            replay();
                            Thread.sleep(BACKOFF);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Interrupted", e);
                        }
                    } else {
                        throw new RuntimeException("Exceeded the number of retries", txEx);
                    }
                }
                throw ((RuntimeException) txEx);
            }

            void addQuery(Neo4jQuery query) {
                this.queries.add(query);
            }

            private void replay() {
                queries.forEach(q -> transaction.run(q.template(), parameters(q.parameters())));
            }

            @Override
            public org.neo4j.driver.Transaction forNeo4j() {
                return transaction;
            }
        }
    }

    public static Result run(DriverWrapper.Session.Transaction transaction, Neo4jQuery query) {
        ((Session.Transaction) transaction).addQuery(query);
        return transaction.forNeo4j().run(query.template(), parameters(query.parameters()));
    }
}
