package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.interaction.EmploymentAgentBase;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.RelocationAgent.cityResidentsQuery;

public class EmploymentAgent extends Neo4jAgent<World.City> implements EmploymentAgentBase {
    @Override
    public List<Long> getCompanyNumbers(World.Country country, String scope, int numCompanies) {
        Query companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(country);
        log().query(scope, companyNumbersQuery);
        return tx().getOrderedAttribute(companyNumbersQuery, "company.companyNumber", numCompanies);
    }

    @Override
    public List<String> getEmployeeEmails(World.City city, String scope, int numEmployments, LocalDateTime earliestDate) {
        Query getEmployeeEmailsQuery = cityResidentsQuery(city, earliestDate);
        log().query(scope, getEmployeeEmailsQuery);
        return tx().getOrderedAttribute(getEmployeeEmailsQuery, "resident.email", numEmployments);
    }

    @Override
    public void insertEmployment(World.City city, String scope, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        String template = "" +
                "MATCH (city:City {locationName: $cityName})-[:LOCATED_IN]->(country:Country),\n" +
                "(person:Person {email: $employeeEmail}),\n" +
                "(company:Company {companyNumber: $companyNumber})\n" +
                "CREATE (company)-[:EMPLOYS {\n" +
                "   wage: $wage,\n" +
                "   currency: country.currency,\n" +
                "   locationName: city.locationName,\n" +
                "   contractContent: $contractContent,\n" +
                "   contractHours: $contractHours}\n" +
                "]->(person)\n";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("cityName", city.name());
                put("employeeEmail", employeeEmail);
                put("companyNumber", companyNumber);
                put("contractContent", contractContent);
                put("contractHours", contractedHours);
                put("wage", wageValue);
        }};

        Query insertEmploymentQuery = new Query(template, parameters);
        log().query(scope, insertEmploymentQuery);
        tx().execute(insertEmploymentQuery);
    }
}
