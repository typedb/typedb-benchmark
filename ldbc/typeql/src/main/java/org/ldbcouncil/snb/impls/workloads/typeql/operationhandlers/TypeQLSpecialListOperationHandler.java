package org.ldbcouncil.snb.impls.workloads.typeql.operationhandlers;

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.answer.ConceptMap;
import org.ldbcouncil.snb.driver.DbException;
import org.ldbcouncil.snb.driver.Operation;
import org.ldbcouncil.snb.driver.ResultReporter;
import org.ldbcouncil.snb.impls.workloads.operationhandlers.ListOperationHandler;
import org.ldbcouncil.snb.impls.workloads.typeql.TypeQLDbConnectionState;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class TypeQLSpecialListOperationHandler<TOperation extends Operation<List<TOperationResult>>, TOperationResult>
        implements ListOperationHandler<TOperationResult,TOperation,TypeQLDbConnectionState>
{
    public TOperationResult toResult(ConceptMap concept) throws ParseException  {
        throw new UnsupportedOperationException("This operation is not supported by the query operation.");
    }

    public abstract Map<String, Object> getParameters(TypeQLDbConnectionState<?> state, TOperation operation );

    public void executeOperation(TOperation operation, TypeQLDbConnectionState state,
                                 ResultReporter resultReporter) throws DbException {
        throw new UnsupportedOperationException("Execution not implemented by query operation.");
    }
}
