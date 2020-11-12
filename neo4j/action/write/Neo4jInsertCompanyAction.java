package grakn.simulation.neo4j.action.write;

import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.write.InsertCompanyAction;
import grakn.simulation.common.world.World;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.neo4j.action.Model.COMPANY_NAME;
import static grakn.simulation.neo4j.action.Model.COMPANY_NUMBER;
import static grakn.simulation.neo4j.action.Model.DATE_OF_INCORPORATION;
import static grakn.simulation.neo4j.action.Model.LOCATION_NAME;

public class Neo4jInsertCompanyAction extends InsertCompanyAction<Neo4jOperation, Record> {

    public Neo4jInsertCompanyAction(Neo4jOperation dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        super(dbOperation, country, today, companyNumber, companyName);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("countryName", country.name());
            put("companyNumber", companyNumber);
            put("companyName", companyName);
            put("dateOfIncorporation", today);
        }};
        return Action.singleResult(dbOperation.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH (country:Country {locationName: $countryName})\n" +
                "CREATE (company:Company {companyNumber: $companyNumber, companyName: $companyName})-[incorporation:INCORPORATED_IN {dateOfIncorporation: $dateOfIncorporation}]->(country)" +
                "RETURN company.companyName, company.companyNumber, country.locationName, incorporation.dateOfIncorporation";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertCompanyActionField.COMPANY_NAME, answer.asMap().get("company." + COMPANY_NAME));
                put(InsertCompanyActionField.COMPANY_NUMBER, answer.asMap().get("company." + COMPANY_NUMBER));
                put(InsertCompanyActionField.COUNTRY, answer.asMap().get("country." + LOCATION_NAME));
                put(InsertCompanyActionField.DATE_OF_INCORPORATION, answer.asMap().get("incorporation." + DATE_OF_INCORPORATION));
            }
        };
    }
}
