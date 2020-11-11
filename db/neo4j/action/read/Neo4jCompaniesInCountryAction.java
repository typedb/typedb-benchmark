package grakn.simulation.db.neo4j.action.read;

import grakn.simulation.common.action.read.CompaniesInCountryAction;
import grakn.simulation.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

public class Neo4jCompaniesInCountryAction extends CompaniesInCountryAction<Neo4jOperation> {
    public Neo4jCompaniesInCountryAction(Neo4jOperation dbOperation, World.Country country, int numCompanies) {
        super(dbOperation, country, numCompanies);
    }

    @Override
    public List<Long> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("countryName", country.name());
        }};
        return dbOperation.sortedExecute(new Query(query(), parameters), "company.companyNumber", numCompanies);
    }

    public static String query() {
        return "MATCH (company:Company)-[:INCORPORATED_IN]->(country:Country {locationName: $countryName})\n" +
                "RETURN company.companyNumber";
    }
}
