package org.ldbcouncil.snb.impls.workloads.typeql.operationhandlers;

import org.ldbcouncil.snb.driver.DbException;
import org.ldbcouncil.snb.driver.Operation;
import org.ldbcouncil.snb.driver.ResultReporter;
import org.ldbcouncil.snb.impls.workloads.typeql.TypeQLDbConnectionState;
import org.ldbcouncil.snb.impls.workloads.operationhandlers.SingletonOperationHandler;

import com.vaticle.typedb.driver.api.answer.JSON;
import com.vaticle.typedb.driver.api.TypeDBTransaction;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

public abstract class TypeQLSingletonOperationHandler<TOperation extends Operation<TOperationResult>, TOperationResult>
        implements SingletonOperationHandler<TOperationResult,TOperation,TypeQLDbConnectionState<?>>
{
    public abstract TOperationResult toResult(JSON concept) throws ParseException;

    public abstract Map<String, Object> getParameters(TypeQLDbConnectionState<?> state, TOperation operation );

    @Override
    public void executeOperation(TOperation operation, TypeQLDbConnectionState<?> state,
                                 ResultReporter resultReporter) throws DbException
    {
        System.out.println("Executing operation: " + operation.getClass().getSimpleName());
        String query = getQueryString(state, operation);
        final Map<String, Object> parameters = getParameters(state, operation);

        try(TypeDBTransaction transaction = state.getTransaction()){
                //replace parameters in query
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    query = query.replace(entry.getKey(), entry.getValue().toString());
                }

                Iterator<JSON> result = transaction.query().fetch(query).iterator();

                if (result.hasNext()) {
                    resultReporter.report(1, toResult(result.next()), operation);
                } else {
                    resultReporter.report(0, toResult(null), operation);
                }
        }
        catch (ParseException e) {
            throw new DbException(e);
        }
    }
}
