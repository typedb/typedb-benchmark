package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.CompanyAgentBase;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grakn.simulation.db.neo4j.schema.Schema.COMPANY_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.neo4j.schema.Schema.DATE_OF_INCORPORATION;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;

public class CompanyAgent extends Neo4jAgent<World.Country> implements CompanyAgentBase {
    @Override
    public AgentResult insertCompany(World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        String template = "" +
                "MATCH (country:Country {locationName: $countryName})\n" +
                "CREATE (company:Company {companyNumber: $companyNumber, companyName: $companyName})-[incorporation:INCORPORATED_IN {dateOfIncorporation: $dateOfIncorporation}]->(country)" +
                "RETURN company.companyName, company.companyNumber, country.locationName, incorporation.dateOfIncorporation";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("countryName", country.name());
                put("companyNumber", companyNumber);
                put("companyName", companyName);
                put("dateOfIncorporation", today);
        }};
        Query companyQuery = new Query(template, parameters);
        return single_result(tx().execute(companyQuery));
    }

    @Override
    public AgentResult resultsForTesting(Record answer) {
        return new AgentResult() {
            {
                put(CompanyAgentField.COMPANY_NAME, answer.asMap().get("company." + COMPANY_NAME));
                put(CompanyAgentField.COMPANY_NUMBER, answer.asMap().get("company." + COMPANY_NUMBER));
                put(CompanyAgentField.COUNTRY, answer.asMap().get("country." + LOCATION_NAME));
                put(CompanyAgentField.DATE_OF_INCORPORATION, answer.asMap().get("incorporation." + DATE_OF_INCORPORATION));
            }
        };
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
