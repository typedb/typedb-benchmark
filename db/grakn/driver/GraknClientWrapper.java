package grakn.simulation.db.grakn.driver;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.driver.DriverWrapper;
import graql.lang.query.GraqlGet;

import java.util.List;
import java.util.stream.Collectors;

public class GraknClientWrapper implements DriverWrapper<GraknClientWrapper.Session, GraknClientWrapper.Transaction> {

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

    public static class Session extends DriverWrapper.Session<Session, Transaction> {

        private GraknClient.Session session;

        Session(GraknClient.Session session) {
            this.session = session;
        }

        @Override
        public void close() {
            this.session.close();
        }

        @Override
        public GraknClientWrapper.Transaction transaction() {
            return new GraknClientWrapper.Transaction(session.transaction(GraknClient.Transaction.Type.WRITE));
        }

    }

    public static class Transaction extends DriverWrapper.Transaction<GraqlGet, List<ConceptMap>> {

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

        public GraknClient.Transaction forGrakn() {
            return transaction;
        }

        @Override
        public List<ConceptMap> run(GraqlGet query) {
            return transaction.execute(query).get();
        }

        public <T> List<T> getOrderedAttribute(GraqlGet query, String attributeName, Integer limit){
            return transaction.stream(query).get()
                    .map(conceptMap -> (T) conceptMap.get(attributeName).asAttribute().value())
                    .sorted()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

    }
}
