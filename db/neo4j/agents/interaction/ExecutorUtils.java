package grakn.simulation.db.neo4j.agents.interaction;

import java.util.List;
import java.util.stream.Collectors;

import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

/**
 * Provide helpers to execute match get queries and retrieve their answers in a deterministic order
 */
public class ExecutorUtils {

    @SuppressWarnings("unchecked")
    public static <T> List<T> getOrderedAttribute(org.neo4j.driver.Transaction tx, Neo4jQuery neo4jQuery, String attributeName, Integer limit){
        return run(tx, neo4jQuery).stream()
                .map(record -> (T) record.asMap().get(attributeName))
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getOrderedAttribute(org.neo4j.driver.Transaction tx, Neo4jQuery neo4jQuery, String attributeName){
        return run(tx, neo4jQuery).stream()
                .map(record -> (T) record.asMap().get(attributeName))
                .sorted()
                .collect(Collectors.toList());
    }

}