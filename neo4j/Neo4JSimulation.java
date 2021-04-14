/*
 * Copyright (C) 2021 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.neo4j;

import grakn.benchmark.common.Config;
import grakn.benchmark.neo4j.agent.Neo4jAgeUpdateAgent;
import grakn.benchmark.neo4j.agent.Neo4jArbitraryOneHopAgent;
import grakn.benchmark.neo4j.agent.Neo4jCompanyAgent;
import grakn.benchmark.neo4j.agent.Neo4jEmploymentAgent;
import grakn.benchmark.neo4j.agent.Neo4jFindCurrentResidentsAgent;
import grakn.benchmark.neo4j.agent.Neo4jFindLivedInAgent;
import grakn.benchmark.neo4j.agent.Neo4jFindSpecificMarriageAgent;
import grakn.benchmark.neo4j.agent.Neo4jFindSpecificPersonAgent;
import grakn.benchmark.neo4j.agent.Neo4jFindTransactionCurrencyAgent;
import grakn.benchmark.neo4j.agent.Neo4jFourHopAgent;
import grakn.benchmark.neo4j.agent.Neo4jFriendshipAgent;
import grakn.benchmark.neo4j.agent.Neo4jMarriageAgent;
import grakn.benchmark.neo4j.agent.Neo4jMeanWageAgent;
import grakn.benchmark.neo4j.agent.Neo4jParentshipAgent;
import grakn.benchmark.neo4j.agent.Neo4jPersonBirthAgent;
import grakn.benchmark.neo4j.agent.Neo4jProductAgent;
import grakn.benchmark.neo4j.agent.Neo4jPurchaseAgent;
import grakn.benchmark.neo4j.agent.Neo4jRelocationAgent;
import grakn.benchmark.neo4j.agent.Neo4jThreeHopAgent;
import grakn.benchmark.neo4j.agent.Neo4jTwoHopAgent;
import grakn.benchmark.neo4j.driver.Neo4jClient;
import grakn.benchmark.neo4j.driver.Neo4jSession;
import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.Simulation;
import grakn.benchmark.simulation.agent.Agent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static grakn.benchmark.common.Util.printDuration;

public class Neo4JSimulation extends Simulation<Neo4jClient, Neo4jSession, Neo4jTransaction> {

    private static final Logger LOG = LoggerFactory.getLogger(Neo4JSimulation.class);

    private Neo4JSimulation(Neo4jClient driver, int randomSeed, List<Config.Agent> agentConfigs, SimulationContext context) throws Exception {
        super(driver, randomSeed, agentConfigs, context);
    }

    public static Neo4JSimulation create(String hostUri, int randomSeed, List<Config.Agent> agentConfigs, SimulationContext context) throws Exception {
        return new Neo4JSimulation(new Neo4jClient(hostUri), randomSeed, agentConfigs, context);
    }

    @Override
    protected void initialiseDatabase() {
        try (org.neo4j.driver.Session session = client().unpack().session()) {
            addKeyConstraints(session);
            cleanDatabase(session);
        }
    }

    private void cleanDatabase(Session session) {
        Transaction tx = session.beginTransaction();
        tx.run(new Query("MATCH (n) DETACH DELETE n"));
        tx.commit();
    }

    /**
     * Neo4j Community can create only uniqueness constraints, and only on nodes, not relationships. This means that it
     * does not enforce the existence of a property on those nodes. `exists()` is only available in Neo4j Enterprise.
     * https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
     *
     * @param session
     */
    private void addKeyConstraints(Session session) {
        List<String> queries = new ArrayList<>() {{
            add("CREATE CONSTRAINT unique_person_email ON (person:Person) ASSERT person.email IS UNIQUE");
            add("CREATE CONSTRAINT unique_location_locationName ON (location:Location) ASSERT location.locationName IS UNIQUE");
            add("CREATE CONSTRAINT unique_company_companyName ON (company:Company) ASSERT company.companyName IS UNIQUE");
            add("CREATE CONSTRAINT unique_company_companyNumber ON (company:Company) ASSERT company.companyNumber IS UNIQUE");
            add("CREATE CONSTRAINT unique_product_productBarcode ON (product:Product) ASSERT product.productBarcode IS UNIQUE");
        }};
        Transaction tx = session.beginTransaction();
        for (String query : queries) {
            tx.run(new Query(query));
        }
        tx.commit();
    }

    @Override
    protected void initialiseData(GeoData geoData) {
        try (org.neo4j.driver.Session session = client().unpack().session()) {
            LOG.info("Neo4j initialisation of world simulation data started ...");
            Instant start = Instant.now();
            // TODO: we don't initialise currencies?
            initialiseContinents(session, geoData.continents());
            initialiseCountries(session, geoData.countries());
            initialiseCities(session, geoData.cities());
            LOG.info("Neo4j initialisation of world simulation data ended in {}", printDuration(start, Instant.now()));
        }
    }

    private void initialiseContinents(Session session, List<GeoData.Continent> continents) {
        Transaction tx = session.beginTransaction();
        continents.forEach(continent -> {
            Query interpolatedQuery = new Query(String.format(
                    "CREATE (x:Continent:Location {locationName: '%s'})", continent.name()
            ));
            tx.run(interpolatedQuery);
        });
        tx.commit();
    }

    private void initialiseCountries(Session session, List<GeoData.Country> countries) {
        Transaction tx = session.beginTransaction();
        countries.forEach(country -> {
            StringBuilder languageProps = new StringBuilder();
            for (int i = 0; i < country.languages().size(); i++) {
                String language = country.languages().get(i);
                languageProps.append("language").append(i + 1).append(": '").append(language).append("'");
                if (i + 1 < country.languages().size()) languageProps.append(", ");
            }
            Query query = new Query(String.format(
                    "MATCH (c:Continent {locationName: '%s'}) CREATE (x:Country:Location {locationName: '%s', currency: '%s', %s})-[:LOCATED_IN]->(c)",
                    country.continent().name(), country.name(), country.currency().name(), languageProps.toString()
            ));
            tx.run(query);
        });
        tx.commit();
    }

    private void initialiseCities(Session session, List<GeoData.City> cities) {
        Transaction tx = session.beginTransaction();
        cities.forEach(city -> {
            Query query = new Query(String.format(
                    "MATCH (c:Country {locationName: '%s'}) CREATE (x:City:Location {locationName: '%s'})-[:LOCATED_IN]->(c)",
                    city.country().name(), city.name()
            ));
            tx.run(query);
        });
        tx.commit();
    }

    @Override
    protected Agent<?, Neo4jTransaction> createAgeUpdateAgent() {
        return new Neo4jAgeUpdateAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createArbitraryOneHopAgent() {
        return new Neo4jArbitraryOneHopAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createCompanyAgent() {
        return new Neo4jCompanyAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createEmploymentAgent() {
        return new Neo4jEmploymentAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createFindCurrentResidentsAgent() {
        return new Neo4jFindCurrentResidentsAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createFindLivedInAgent() {
        return new Neo4jFindLivedInAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createFindSpecificMarriageAgent() {
        return new Neo4jFindSpecificMarriageAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createFindSpecificPersonAgent() {
        return new Neo4jFindSpecificPersonAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createFindTransactionCurrencyAgent() {
        return new Neo4jFindTransactionCurrencyAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createFourHopAgent() {
        return new Neo4jFourHopAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createFriendshipAgent() {
        return new Neo4jFriendshipAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createMarriageAgent() {
        return new Neo4jMarriageAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createMeanWageAgent() {
        return new Neo4jMeanWageAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createParentshipAgent() {
        return new Neo4jParentshipAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createPersonBirthAgent() {
        return new Neo4jPersonBirthAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createProductAgent() {
        return new Neo4jProductAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createPurchaseAgent() {
        return new Neo4jPurchaseAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createRelocationAgent() {
        return new Neo4jRelocationAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createThreeHopAgent() {
        return new Neo4jThreeHopAgent(client(), context());
    }

    @Override
    protected Agent<?, Neo4jTransaction> createTwoHopAgent() {
        return new Neo4jTwoHopAgent(client(), context());
    }
}
