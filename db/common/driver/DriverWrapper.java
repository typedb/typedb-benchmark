package grakn.simulation.db.common.driver;

import grakn.client.GraknClient;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface DriverWrapper extends AutoCloseable {

    DriverWrapper open(String uri);
    void close();
    Session session(String database);

    enum TracingLabel {
        OPEN_CLIENT("openClient"),
        CLOSE_CLIENT("closeClient"),
        OPEN_SESSION("openSession"),
        CLOSE_SESSION("closeSession"),
        OPEN_TRANSACTION("openTx"),
        CLOSE_TRANSACTION("closeTx"),
        COMMIT_TRANSACTION("commitTx"),
        EXECUTE("execute"),
        STREAM_AND_SORT("streamAndSort");

        private String name;

        TracingLabel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    abstract class Session {
        public abstract void close();
        public void closeWithTracing() {
            try (ThreadTrace trace = traceOnThread(TracingLabel.CLOSE_SESSION.getName())) {
                close();
            }
        }
        public abstract Transaction transaction();
        public Transaction transactionWithTracing() {
            try (ThreadTrace trace = traceOnThread(TracingLabel.OPEN_TRANSACTION.getName())) {
                return transaction();
            }
        }

        public abstract class Transaction implements AutoCloseable {
            public abstract void close();
            public void closeWithTracing() {
                try (ThreadTrace trace = traceOnThread(TracingLabel.CLOSE_TRANSACTION.getName())) {
                    close();
                }
            }
            public abstract void commit();
            public void commitWithTracing() {
                try (ThreadTrace trace = traceOnThread(TracingLabel.COMMIT_TRANSACTION.getName())) {
                    commit();
                }
            }
            public GraknClient.Transaction forGrakn() {
                throw new ClassCastException("Can't cast transaction into a Grakn transaction");
            }
            public org.neo4j.driver.Transaction forNeo4j() {
                throw new ClassCastException("Can't cast transaction into a Neo4j transaction");
            }
//            TODO Add these functionalities abstractly
//            public abstract QueryResult execute(Query query);
//            public QueryResult executeWithTracing(Query query) {
//                try (ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
//                    return execute(query);
//                }
//            }
//
//            public abstract <T> List<T> getOrderedAttribute(Query query, String attributeName, Integer limit);
//            public <T> List<T> getOrderedAttributeWithTracing(Query query, String attributeName, Integer limit) {
//                try (ThreadTrace trace = traceOnThread(STREAM_AND_SORT.getName())) {
//                    return getOrderedAttribute(query, attributeName, limit);
//                }
//            }
        }
    }
}
