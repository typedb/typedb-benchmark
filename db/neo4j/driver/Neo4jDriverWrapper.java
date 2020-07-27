package grakn.simulation.db.neo4j.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.driver.DriverWrapper;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.exceptions.TransientException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grabl.tracing.client.GrablTracingThreadStatic.*;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.driver.DriverWrapper.TracingLabel.CLOSE_SESSION;
import static grakn.simulation.db.common.driver.DriverWrapper.TracingLabel.EXECUTE;
import static grakn.simulation.db.common.driver.DriverWrapper.TracingLabel.OPEN_CLIENT;
import static grakn.simulation.db.common.driver.DriverWrapper.TracingLabel.OPEN_SESSION;
import static grakn.simulation.db.common.driver.DriverWrapper.TracingLabel.OPEN_TRANSACTION;
import static grakn.simulation.db.common.driver.DriverWrapper.TracingLabel.STREAM_AND_SORT;

public class Neo4jDriverWrapper implements DriverWrapper {

    private Driver driver = null;

    public Driver getClient() {
        return driver;
    }

    @Override
    public Neo4jDriverWrapper open(String uri) {
        try (ThreadTrace trace = traceOnThread(OPEN_CLIENT.getName())) {
            driver = GraphDatabase.driver(uri, AuthTokens.basic("neo4j", "admin"));
        }
        return this;
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public Session session(String database) {
        try (ThreadTrace trace = traceOnThread(OPEN_SESSION.getName())) {
            return new Session(driver.session());
        }
    }

    public static class Session extends DriverWrapper.Session {

        private org.neo4j.driver.Session session;

        Session(org.neo4j.driver.Session session) {
            this.session = session;
        }

        @Override
        public void close() {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(CLOSE_SESSION.getName())) {
                this.session.close();
            }
        }

        @Override
        public Transaction transaction() {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_TRANSACTION.getName())) {
                return new Transaction(session);
            }
        }

        public class Transaction extends DriverWrapper.Session.Transaction {

            private org.neo4j.driver.Transaction transaction;
            private List<Query> queries = new ArrayList<Query>();
            private org.neo4j.driver.Session session;

            Transaction(org.neo4j.driver.Session session) {
                this.session = session;
                this.transaction = newTransaction();
            }

            private org.neo4j.driver.Transaction newTransaction() {
                try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_TRANSACTION.getName())) {
                    return session.beginTransaction();
                }
            }

            @Override
            public void close() {
                try (ThreadTrace trace = traceOnThread("close")) {
                    transaction.close();
                }
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

            void addQuery(Query query) {
                this.queries.add(query);
            }

            private void replay() {
                queries.forEach(q -> transaction.run(q));
            }

            @Override
            public org.neo4j.driver.Transaction forNeo4j() {
                return transaction;
            }

            public Result execute(Query query) {
                addQuery(query);
                try (ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
                    return transaction.run(query);
                }
            }

            public <T> List<T> getOrderedAttribute(Query query, String attributeName, Integer limit) {
                List<T> result;
                try (ThreadTrace trace = traceOnThread(STREAM_AND_SORT.getName())) {
                    Stream<T> answerStream = execute(query).stream()
                            .map(record -> (T) record.asMap().get(attributeName))
                            .sorted();
                    if (limit != null) {
                        answerStream = answerStream.limit(limit);
                    }
                    result = answerStream.collect(Collectors.toList());
                }
                return result;
            }
        }
    }
}
