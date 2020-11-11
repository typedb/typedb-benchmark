package grakn.simulation.db.neo4j.action.write;

import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.write.InsertMarriageAction;
import grakn.simulation.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.HashMap;

import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.MARRIAGE_ID;

public class Neo4jInsertMarriageAction extends InsertMarriageAction<Neo4jOperation, Record> {

    public Neo4jInsertMarriageAction(Neo4jOperation dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        super(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put(MARRIAGE_ID, marriageIdentifier);
            put("wifeEmail", wifeEmail);
            put("husbandEmail", husbandEmail);
            put(LOCATION_NAME, worldCity.name());
        }};
        return Action.singleResult(dbOperation.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH (wife:Person {email: $wifeEmail}), (husband:Person {email: $husbandEmail}), (city:City {locationName: $locationName})\n" +
                "CREATE (husband)-[marriage:MARRIED_TO {marriageId: $marriageId, locationName: city.locationName}]->(wife)" +
                "RETURN marriage.marriageId, husband.email, wife.email, city.locationName";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>(){{
            put(InsertMarriageActionField.MARRIAGE_IDENTIFIER, answer.asMap().get("marriage." + MARRIAGE_ID));
            put(InsertMarriageActionField.WIFE_EMAIL, answer.asMap().get("wife." + EMAIL));  // TODO we get back the variables matched for in an insert?
            put(InsertMarriageActionField.HUSBAND_EMAIL, answer.asMap().get("husband." + EMAIL));
            put(InsertMarriageActionField.CITY_NAME, answer.asMap().get("city." + LOCATION_NAME));
        }};
    }
}
