package grakn.simulation.db.common.driver;

public interface DbTransaction {
    void close();
    void commit();
}
