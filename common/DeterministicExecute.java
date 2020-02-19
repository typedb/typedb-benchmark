package grakn.simulation.common;

import grakn.client.GraknClient;
import graql.lang.query.GraqlGet;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provide helpers to execute match get queries and retrieve their answers in a deterministic order
 */
public class DeterministicExecute {
    public static <T> List<T> getAttributeAnswers(GraknClient.Transaction tx, GraqlGet query, String attributeName, Integer limit){
        return tx.execute(query)
                .stream()
                .map(conceptMap -> (T) conceptMap.get(attributeName).asAttribute().value())
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
    }
}