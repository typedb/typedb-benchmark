package grakn.simulation.db.common.driver;

import java.util.List;

public interface DriverWrapper<S extends DriverWrapper.Session, T extends DriverWrapper.Transaction> extends AutoCloseable {

    DriverWrapper open(String uri);
    void close();
    Session session(String database);

    abstract class Session<S extends DriverWrapper.Session<S, T>, T extends DriverWrapper.Transaction> {
        public abstract void close();
        public abstract T transaction();
    }

    public abstract static class Transaction<Query, Answer> implements AutoCloseable {
        public abstract void close();
        public abstract void commit();
        public abstract Answer run(Query query);
        public abstract <T> List<T> getOrderedAttribute(Query query, String attributeName, Integer limit);
    }
}
