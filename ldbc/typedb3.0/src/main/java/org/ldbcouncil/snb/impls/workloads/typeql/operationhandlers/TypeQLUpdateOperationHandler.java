package org.ldbcouncil.snb.impls.workloads.typeql.operationhandlers;

import com.vaticle.typedb.driver.api.answer.ConceptMap;
import org.ldbcouncil.snb.driver.DbException;
import org.ldbcouncil.snb.driver.Operation;
import org.ldbcouncil.snb.driver.ResultReporter;
import org.ldbcouncil.snb.driver.workloads.interactive.queries.LdbcNoResult;
import org.ldbcouncil.snb.impls.workloads.operationhandlers.ListOperationHandler;
import org.ldbcouncil.snb.impls.workloads.operationhandlers.UpdateOperationHandler;
import org.ldbcouncil.snb.impls.workloads.typeql.TypeQLDbConnectionState;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public abstract class TypeQLUpdateOperationHandler<TOperation extends Operation<LdbcNoResult>>
        implements UpdateOperationHandler<TOperation,TypeQLDbConnectionState>
{
    @Override
    public String getQueryString( TypeQLDbConnectionState state, TOperation operation )
    {
        return null;
    }

    public abstract Map<String, Object> getParameters(TOperation operation );


    @Override
    public void executeOperation( TOperation operation, TypeQLDbConnectionState state,
                                  ResultReporter resultReporter ) throws DbException {
        throw new UnsupportedOperationException("Execution not implemented by query operation.");
    }
}