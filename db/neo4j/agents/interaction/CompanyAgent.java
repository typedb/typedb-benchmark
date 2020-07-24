package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import org.neo4j.driver.Query;

import java.util.HashMap;

public class CompanyAgent extends grakn.simulation.db.common.agents.interaction.CompanyAgent {
    @Override
    protected void insertCompany(int companyNumber, String companyName) {
        String template = "" +
                "MATCH (country:Country {locationName: $countryName})\n" +
                "CREATE (country)-[:INCORPORATED_IN {dateOfIncorporation: $dateOfIncorporation}]->(company:Company {companyNumber: $companyNumber, companyName: $companyName})";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("countryName", country().name());
                put("companyNumber", companyNumber);
                put("companyName", companyName);
                put("dateOfIncorporation", today());
        }};

        Query companyQuery = new Query(template, parameters);
        ((Neo4jDriverWrapper.Session.Transaction) tx()).run(companyQuery);
    }

    static Query getCompanyNumbersInContinentQuery(World.Continent continent) {
        String template = "" +
                "MATCH (company:Company)-[:INCORPORATED_IN]->(country:Country)-[:LOCATED_IN]->(continent:Continent {locationName: $continentName})\n" +
                "RETURN company.companyNumber";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("continentName", continent.name());
        }};
        return new Query(template, parameters);
    }

    static Query getCompanyNumbersInCountryQuery(World.Country country) {
        String template = "" +
                "MATCH (company:Company)-[:INCORPORATED_IN]->(country:Country {locationName: $countryName})\n" +
                "RETURN company.companyNumber";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("countryName", country.name());
        }};
        return new Query(template, parameters);
    }
}
