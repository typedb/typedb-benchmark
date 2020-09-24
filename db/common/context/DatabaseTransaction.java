package grakn.simulation.db.common.context;

public interface DatabaseTransaction {
    void close();
    void commit();
}
