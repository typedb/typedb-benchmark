package grakn.simulation.db.neo4j.action.write;

import grakn.simulation.db.common.action.write.InsertEmploymentAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.db.neo4j.schema.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.neo4j.schema.Schema.CONTRACTED_HOURS;
import static grakn.simulation.db.neo4j.schema.Schema.CONTRACT_CONTENT;
import static grakn.simulation.db.neo4j.schema.Schema.CURRENCY;
import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.START_DATE;
import static grakn.simulation.db.neo4j.schema.Schema.WAGE;

public class Neo4jInsertEmploymentAction extends InsertEmploymentAction<Neo4jOperation, Record> {
    public Neo4jInsertEmploymentAction(Neo4jOperation dbOperation, World.City worldCity, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        super(dbOperation, worldCity, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    @Override
    public Record run() {
        String template = "" +
                "MATCH (city:City {locationName: $locationName})-[:LOCATED_IN]->(country:Country),\n" +
                "(person:Person {email: $email}),\n" +
                "(company:Company {companyNumber: $companyNumber})\n" +
                "CREATE (company)-[employs:EMPLOYS {\n" +
                "   startDate: $startDate,\n" +
                "   wage: $wage,\n" +
                "   currency: country.currency,\n" +
                "   locationName: city.locationName,\n" +
                "   contractContent: $contractContent,\n" +
                "   contractedHours: $contractedHours}\n" +
                "]->(person)\n" +
                "RETURN city.locationName, person.email, company.companyNumber, country.locationName, \n" +
                "employs.startDate, employs.wage, employs.currency, employs.contractContent, employs.contractedHours";

        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("locationName", worldCity.name());
            put("email", employeeEmail);
            put("companyNumber", companyNumber);
            put("startDate", employmentDate);
            put("wage", wageValue);
            put("contractContent", contractContent);
            put("contractedHours", contractedHours);
        }};
        return singleResult(dbOperation.execute(new Query(template, parameters)));
    }

    @Override
    public HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertEmploymentActionField.CITY_NAME, answer.asMap().get("city." + LOCATION_NAME));
            put(InsertEmploymentActionField.PERSON_EMAIL, answer.asMap().get("person." + EMAIL));
            put(InsertEmploymentActionField.COMPANY_NUMBER, answer.asMap().get("company." + COMPANY_NUMBER));
            put(InsertEmploymentActionField.START_DATE, answer.asMap().get("employs." + START_DATE));
            put(InsertEmploymentActionField.WAGE, answer.asMap().get("employs." + WAGE));
            put(InsertEmploymentActionField.CURRENCY, answer.asMap().get("employs." + CURRENCY));
            put(InsertEmploymentActionField.CONTRACT_CONTENT, answer.asMap().get("employs." + CONTRACT_CONTENT));
            put(InsertEmploymentActionField.CONTRACTED_HOURS, answer.asMap().get("employs." + CONTRACTED_HOURS));
        }};
    }
}
