package grakn.simulation.db.neo4j.agents.interaction;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class EmploymentAgent extends grakn.simulation.db.common.agents.interaction.EmploymentAgent {
    @Override
    protected List<Long> getCompanyNumbers() {
        Neo4jQuery companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(city().country());
        log().query("getEmployeeEmails", companyNumbersQuery);
        int numCompanies = world().getScaleFactor();
        return ExecutorUtils.getOrderedAttribute(tx().forNeo4j(), companyNumbersQuery, "company.companyNumber", numCompanies);
    }

    @Override
    protected List<String> getEmployeeEmails(LocalDateTime earliestDate) {
        Neo4jQuery getEmployeeEmailsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getEmployeeEmails", getEmployeeEmailsQuery);
        int numEmployments = world().getScaleFactor();
        return ExecutorUtils.getOrderedAttribute(tx().forNeo4j(), getEmployeeEmailsQuery, "resident.email", numEmployments);
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

        Object[] parameters = new Object[] {
                "cityName", city().name(),
                "employeeEmail", employeeEmail,
                "companyNumber", companyNumber,
                "contractContent", contractContent,
                "contractHours", contractedHours,
                "wage", wageValue
        };

        Neo4jQuery insertEmploymentQuery = new Neo4jQuery(template, parameters);
        log().query("insertEmployment", insertEmploymentQuery);
        run(tx().forNeo4j(), insertEmploymentQuery);
    }
}
