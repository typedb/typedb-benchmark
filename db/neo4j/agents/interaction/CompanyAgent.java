package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.world.World;

import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class CompanyAgent extends grakn.simulation.db.common.agents.interaction.CompanyAgent {
    @Override
    protected void insertCompany(int companyNumber, String companyName) {
        String template = "" +
                "MATCH (country:Country {locationName: $countryName})\n" +
                "CREATE (country)-[:INCORPORATED_IN {dateOfIncorporation: $dateOfIncorporation}]->(company:Company {companyNumber: $companyNumber, companyName: $companyName})";

        Object[] parameters = new Object[]{
                "countryName", country().name(),
                "companyNumber", companyNumber,
                "companyName", companyName,
                "dateOfIncorporation", today()
        };

        Neo4jQuery companyQuery = new Neo4jQuery(template, parameters);
        run(tx().forNeo4j(), companyQuery);
    }

    static Neo4jQuery getCompanyNumbersInCountryQuery(World.Country country) {
        String template = "" +
                "MATCH (country:Country {locationName: $countryName})-[:INCORPORATED_IN]->(company:Company)\n" +
                "RETURN company.companyNumber";

        Object[] parameters = new Object[]{
                "countryName", country.name(),
        };
        return new Neo4jQuery(template, parameters);
    }
}
