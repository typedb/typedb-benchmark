package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.db.common.action.insight.MeanWageOfPeopleInWorldAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;

public class Neo4jMeanWageOfPeopleInWorldAction extends MeanWageOfPeopleInWorldAction<Neo4jOperation> {

    public Neo4jMeanWageOfPeopleInWorldAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public Double run() {
        String template = "" +
                "MATCH ()-[employs:EMPLOYS]->()\n" +
                "RETURN avg(employs.wage)\n";
        List<Record> records = dbOperation.execute(new Query(template));
        return (Double) getOnlyElement(records).asMap().get("avg(employs.wage)");
    }
}
