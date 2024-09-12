package org.ldbcouncil.snb.impls.workloads.typeql;

import org.ldbcouncil.snb.driver.DbException;
import org.ldbcouncil.snb.impls.workloads.BaseDbConnectionState;
import org.ldbcouncil.snb.impls.workloads.QueryStore;

import com.vaticle.typedb.driver.TypeDB;
import com.vaticle.typedb.driver.api.TypeDBDriver;
import com.vaticle.typedb.driver.api.TypeDBSession;
import com.vaticle.typedb.driver.api.TypeDBTransaction;

import java.util.Map;

public class TypeQLDbConnectionState<TDbQueryStore extends QueryStore> extends BaseDbConnectionState<TDbQueryStore> {

    private TypeDBDriver driver;
    private TypeDBSession session;
    private String dbName;
    private String endpoint;

    public TypeQLDbConnectionState(Map<String, String> properties, TDbQueryStore store) throws ClassNotFoundException  {
        super(properties, store);
        endpoint = properties.getOrDefault("endpoint", TypeDB.DEFAULT_ADDRESS);
        dbName = properties.getOrDefault("databaseName", "ldbcsnb");
        driver = null;
        session = null;
    }

    public TypeDBDriver getDriver() throws DbException {
        if (driver == null || !driver.isOpen()) {
            driver = TypeDB.coreDriver(endpoint);
        }
        return driver;
    }
    

    public TypeDBTransaction getTransaction() throws DbException {
        if (session == null || !session.isOpen()) {
            session = getDriver().session(dbName, TypeDBSession.Type.DATA);
        }
        TypeDBTransaction transaction = session.transaction(TypeDBTransaction.Type.READ);
        return transaction;
    }

    @Override
    public void close() {
        if (session != null && session.isOpen()) {
            session.close();
        }
        if (driver != null && driver.isOpen()) {
            driver.close();
        }
    }
}
