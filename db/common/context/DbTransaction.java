package grakn.simulation.db.common.context;

public interface DbTransaction {
    void close();
    void commit();
}
