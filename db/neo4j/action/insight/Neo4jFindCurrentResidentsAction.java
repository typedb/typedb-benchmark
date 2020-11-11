package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.common.action.insight.FindCurrentResidentsAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;

public class Neo4jFindCurrentResidentsAction extends FindCurrentResidentsAction<Neo4jOperation> {
    public Neo4jFindCurrentResidentsAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(new Query(query()), "email", null);
    }

    public static String query() {
//        Finds only those who currently live in Berlin
//        This means those who were born in Berlin and never relocated Berlin, or whose last relocation was to Berlin
        return "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: \"Berlin\"})\n" +
                "WHERE NOT (person)-[:RELOCATED_TO]->()\n" +
                "RETURN person.email AS email\n" +
                "UNION\n" +
                "MATCH (person:Person)-[relocatedTo:RELOCATED_TO]->(city:City)\n" +
                "WITH person, city, relocatedTo.relocationDate AS relocDate\n" +
                "ORDER BY relocDate DESC\n" +
                "WITH person.email AS email, collect(relocDate)[0] AS lastRelocDate, collect(city)[0] as lastCity\n" +
                "WHERE lastCity.locationName = \"Berlin\"\n" +
                "RETURN email;";
    }
}
