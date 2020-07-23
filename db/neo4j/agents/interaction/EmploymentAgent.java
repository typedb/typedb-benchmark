package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.RelocationAgent.cityResidentsQuery;

public class EmploymentAgent extends grakn.simulation.db.common.agents.interaction.EmploymentAgent<Neo4jDriverWrapper.Session, Neo4jDriverWrapper.Transaction> {
    @Override
    protected List<Long> getCompanyNumbers() {
        Query companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(city().country());
        log().query("getEmployeeEmails", companyNumbersQuery);
        int numCompanies = world().getScaleFactor();
        return tx().getOrderedAttribute(companyNumbersQuery, "company.companyNumber", numCompanies);
    }

    @Override
    protected List<String> getEmployeeEmails(LocalDateTime earliestDate) {
        Query getEmployeeEmailsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getEmployeeEmails", getEmployeeEmailsQuery);
        int numEmployments = world().getScaleFactor();
        return tx().getOrderedAttribute(getEmployeeEmailsQuery, "resident.email", numEmployments);
    }

    @Override
    protected void insertEmployment(String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
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
                put("cityName", city().name());
                put("employeeEmail", employeeEmail);
                put("companyNumber", companyNumber);
                put("contractContent", contractContent);
                put("contractHours", contractedHours);
                put("wage", wageValue);
        }};

        Query insertEmploymentQuery = new Query(template, parameters);
        log().query("insertEmployment", insertEmploymentQuery);
        tx().run(insertEmploymentQuery);
    }
}
