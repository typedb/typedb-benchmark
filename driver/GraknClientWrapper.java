package grakn.simulation.driver;

import grakn.client.GraknClient;

public class GraknClientWrapper implements DbDriverWrapper {

    private GraknClient client = null;

    public GraknClient getClient() {
        return client;
    }

    @Override
    public void open(String uri) {
        client = new GraknClient(uri);
    }

    @Override
    public void close() {
        client.close();
    }

//    GraknClientWrapper(String uri) {
//        client = new GraknClient(uri);
//    }

    @Override
    public Session session(String keyspace) {
        return new Session(client.session(keyspace));
    }

    class Session implements DbDriverWrapper.Session {

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

        class Transaction implements DbDriverWrapper.Session.Transaction {

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

            @Override
            public org.neo4j.driver.Transaction forNeo4j() {
                throw new ClassCastException("Can't cast a Grakn transation into a Neo4j transaction");
            }
        }
    }
}
