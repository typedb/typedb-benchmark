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

import grakn.benchmark.common.params.Context;
import grakn.benchmark.common.seed.GeoData;
import grakn.benchmark.neo4j.driver.Neo4jClient;
import grakn.benchmark.neo4j.driver.Neo4jSession;
import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.Simulation;
import org.neo4j.driver.Driver;
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
    private final Driver nativeDriver;

    private Neo4JSimulation(Neo4jClient client, Context context) throws Exception {
        super(client, context);
        this.nativeDriver = client.unpack();
    }

    public static Neo4JSimulation create(String hostUri, Context context) throws Exception {
        return new Neo4JSimulation(new Neo4jClient(hostUri), context);
    }

    @Override
    protected void initialise(GeoData geoData) {
        initDatabase();
        initData(geoData);
    }

    private void initDatabase() {
        try (Session session = nativeDriver.session()) {
            addKeyConstraints(session);
            cleanDatabase(session);
        }
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

    private void cleanDatabase(Session session) {
        Transaction tx = session.beginTransaction();
        tx.run(new Query("MATCH (n) DETACH DELETE n"));
        tx.commit();
    }

    private void initData(GeoData geoData) {
        try (Session session = nativeDriver.session()) {
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
}
