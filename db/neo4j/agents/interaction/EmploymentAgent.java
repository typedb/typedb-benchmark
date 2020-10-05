package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.action.Action;
import grakn.simulation.db.common.agents.action.EmployeeEmailsAction;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.neo4j.schema.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.neo4j.schema.Schema.CONTRACTED_HOURS;
import static grakn.simulation.db.neo4j.schema.Schema.CONTRACT_CONTENT;
import static grakn.simulation.db.neo4j.schema.Schema.CURRENCY;
import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.START_DATE;
import static grakn.simulation.db.neo4j.schema.Schema.WAGE;

public class EmploymentAgent extends Neo4jAgent<World.City> implements grakn.simulation.db.common.agents.interaction.EmploymentAgent {
    @Override
    public List<Long> getCompanyNumbers(World.Country country, int numCompanies) {
        Query companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(country);
        return tx().getOrderedAttribute(companyNumbersQuery, "company.companyNumber", numCompanies);
    }

//    @Override
//    public List<String> getEmployeeEmails(World.City city, int numEmployments, LocalDateTime earliestDate) {
//        Query getEmployeeEmailsQuery = cityResidentsQuery(city, earliestDate);
//        return tx().getOrderedAttribute(getEmployeeEmailsQuery, "resident.email", numEmployments);
//    }

//    public GetEmployeeEmailsAction getEmployeeEmails(World.City city, int numEmployments, LocalDateTime earliestDate) {
//        return new GraknGetEmployeeEmailsAction();
//    }

    public class Neo4jEmployeeEmailsAction extends EmployeeEmailsAction {

        Neo4jEmployeeEmailsAction(World.City city, int numEmployments, LocalDateTime earliestDate) {
            super(dbOperation, city, numEmployments, earliestDate);
        }

        @Override
        public List<String> action() {
            Query getEmployeeEmailsQuery = cityResidentsQuery(city, earliestDate);
            return tx().getOrderedAttribute(getEmployeeEmailsQuery, "resident.email", numEmployments);
        }
    }

    @Override
    public ActionResult insertEmployment(World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
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
            put("locationName", city.name());
            put("email", employeeEmail);
            put("companyNumber", companyNumber);
            put("startDate", employmentDate);
            put("wage", wageValue);
            put("contractContent", contractContent);
            put("contractedHours", contractedHours);
        }};
        Query insertEmploymentQuery = new Query(template, parameters);
        return Action.singleResult(tx().execute(insertEmploymentQuery));
    }

    @Override
    public ActionResult resultsForTesting(Record answer) {
        return new ActionResult() {
            {
                put(EmploymentAgentField.CITY_NAME, answer.asMap().get("city." + LOCATION_NAME));
                put(EmploymentAgentField.PERSON_EMAIL, answer.asMap().get("person." + EMAIL));
                put(EmploymentAgentField.COMPANY_NUMBER, answer.asMap().get("company." + COMPANY_NUMBER));
                put(EmploymentAgentField.START_DATE, answer.asMap().get("employs." + START_DATE));
                put(EmploymentAgentField.WAGE, answer.asMap().get("employs." + WAGE));
                put(EmploymentAgentField.CURRENCY, answer.asMap().get("employs." + CURRENCY));
                put(EmploymentAgentField.CONTRACT_CONTENT, answer.asMap().get("employs." + CONTRACT_CONTENT));
                put(EmploymentAgentField.CONTRACTED_HOURS, answer.asMap().get("employs." + CONTRACTED_HOURS));
            }
        };
    }
}
