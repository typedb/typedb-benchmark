package grakn.simulation.db.grakn.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.simulation.db.common.driver.DriverWrapper;
import graql.lang.query.GraqlGet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.driver.DriverWrapper.TracingLabel.STREAM_AND_SORT;

public class GraknClientWrapper implements DriverWrapper {

    private GraknClient client = null;

    public GraknClient getClient() {
        return client;
    }

    @Override
    public GraknClientWrapper open(String uri) {
        client = new GraknClient(uri);
        return this;
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public Session session(String database) {
        return new Session(client.session(database));
    }

    public static class Session extends DriverWrapper.Session {

        private GraknClient.Session session;

        Session(GraknClient.Session session) {
            this.session = session;
        }

        @Override
        public void close() {
            this.session.close();
        }

        @Override
        public Transaction transaction() {
            return new Transaction(session.transaction(GraknClient.Transaction.Type.WRITE));
        }

        public class Transaction extends DriverWrapper.Session.Transaction {

            private GraknClient.Transaction transaction;

            Transaction(GraknClient.Transaction transaction) {
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
            public GraknClient.Transaction forGrakn() {
                return transaction;
            }

            public <T> List<T> getOrderedAttribute(GraqlGet query, String attributeName, Integer limit){
                List<T> result;
                try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(STREAM_AND_SORT.getName())) {
                    Stream<T> answerStream = transaction.stream(query).get()
                            .map(conceptMap -> (T) conceptMap.get(attributeName).asAttribute().value())
                            .sorted();
                    if (limit != null) {
                        answerStream = answerStream.limit(limit);
                    }
                    result = answerStream.collect(Collectors.toList());
                }
                return result;
            }

//            @Override
//            public QueryResult execute(Query query) {
//                return transaction.execute(query.asGraql());
//            }
//
//            @Override
//            public <T> List<T> getOrderedAttribute(Query query, String attributeName, Integer limit) {
//                return null;
//            }
        }
    }
}
