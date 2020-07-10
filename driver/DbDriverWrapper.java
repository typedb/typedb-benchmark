package grakn.simulation.driver;

import grakn.client.GraknClient;

public interface DbDriverWrapper {

    void open(String uri);
    void close();
    Session session(String keyspace);

    interface Session {
        void close();
        Transaction transaction();

        interface Transaction {
            void close();
            void commit();
            GraknClient.Transaction forGrakn();
            org.neo4j.driver.Transaction forNeo4j();
        }
    }

}
