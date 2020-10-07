package grakn.simulation.db.neo4j.action.read;

import grakn.simulation.db.common.action.read.CompanyNumbersAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

public class Neo4jCompaniesInCountryAction extends CompanyNumbersAction<Neo4jOperation> {
    public Neo4jCompaniesInCountryAction(Neo4jOperation dbOperation, World.Country country, int numCompanies) {
        super(dbOperation, country, numCompanies);
    }

    @Override
    public List<Long> run() {
        String template = "" +
                "MATCH (company:Company)-[:INCORPORATED_IN]->(country:Country {locationName: $countryName})\n" +
                "RETURN company.companyNumber";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("countryName", country.name());
        }};
        return dbOperation.getOrderedAttribute(new Query(template, parameters), "company.companyNumber", numCompanies);
    }
}
