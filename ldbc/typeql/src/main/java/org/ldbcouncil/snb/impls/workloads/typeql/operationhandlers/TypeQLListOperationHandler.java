package org.ldbcouncil.snb.impls.workloads.typeql.operationhandlers;

import org.ldbcouncil.snb.driver.DbException;
import org.ldbcouncil.snb.driver.Operation;
import org.ldbcouncil.snb.driver.ResultReporter;
import org.ldbcouncil.snb.impls.workloads.typeql.TypeQLDbConnectionState;
import org.ldbcouncil.snb.impls.workloads.operationhandlers.ListOperationHandler;

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.answer.JSON;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class TypeQLListOperationHandler<TOperation extends Operation<List<TOperationResult>>, TOperationResult>
        implements ListOperationHandler<TOperationResult,TOperation,TypeQLDbConnectionState>
{
    public abstract TOperationResult toResult(JSON concept) throws ParseException;

    public abstract Map<String, Object> getParameters(TypeQLDbConnectionState<?> state, TOperation operation );

    @Override
    public void executeOperation(TOperation operation, TypeQLDbConnectionState state,
                                 ResultReporter resultReporter) throws DbException
    {
        System.out.println("[LOG] Executing operation: " + operation.getClass().getSimpleName());
        String query = getQueryString(state, operation);
        final Map<String, Object> parameters = getParameters(state, operation);
        // Replace parameters in query
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String valueString = entry.getValue().toString().replace("\"", "").replace("\'","");
            query = query.replace(":" + entry.getKey(), valueString);
        }
        System.out.println("[LOG] Query: " + query);
        final List<TOperationResult> results = new ArrayList<>();

        try(TypeDBTransaction transaction = state.getTransaction()){
            System.out.println("[LOG] Transaction: " + transaction);
            
            final Stream<JSON> result = transaction.query().fetch(query);
            
            // Convert and collect results
            result.forEach(concept -> {
                try {
                    results.add(toResult(concept));
                } catch (ParseException e) {
                    // Swallow the error
                    System.err.println("[ERR] Error parsing concept: " + e.getMessage());
                }
            });
            transaction.close();
            resultReporter.report(results.size(), results, operation);
        } catch (Exception e) {
            System.err.println("[ERR] Error executing operation: " + operation.getClass().getSimpleName());
            e.printStackTrace();
        }
    }
}
