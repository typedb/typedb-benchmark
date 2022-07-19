/*
 * Copyright (C) 2022 Vaticle
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

package com.vaticle.typedb.benchmark.neo4j;

import com.vaticle.typedb.benchmark.common.concept.Continent;
import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Currency;
import com.vaticle.typedb.benchmark.common.concept.Global;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.common.seed.SeedData;
import com.vaticle.typedb.benchmark.neo4j.agent.Neo4jFriendshipAgent;
import com.vaticle.typedb.benchmark.neo4j.agent.Neo4jMarriageAgent;
import com.vaticle.typedb.benchmark.neo4j.agent.Neo4jParenthoodAgent;
import com.vaticle.typedb.benchmark.neo4j.agent.Neo4jPersonAgent;
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient;
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jSession;
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction;
import com.vaticle.typedb.benchmark.simulation.Simulation;
import com.vaticle.typedb.benchmark.simulation.agent.CitizenshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.CoupleFriendshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.GrandparenthoodAgent;
import com.vaticle.typedb.benchmark.simulation.agent.LineageAgent;
import com.vaticle.typedb.benchmark.simulation.agent.MaritalStatusAgent;
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent;
import com.vaticle.typedb.benchmark.simulation.agent.NationalityAgent;
import com.vaticle.typedb.benchmark.simulation.agent.ParenthoodAgent;
import com.vaticle.typedb.benchmark.simulation.agent.PersonAgent;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.vaticle.typedb.benchmark.common.Util.printDuration;

public class Neo4JSimulation extends Simulation<Neo4jClient, Neo4jSession, Neo4jTransaction> {

    private static final Logger LOG = LoggerFactory.getLogger(Neo4JSimulation.class);

    private Neo4JSimulation(Neo4jClient client, Context context) throws Exception {
        super(client, context);
    }

    public static Neo4JSimulation create(String hostUri, Context context) throws Exception {
        return new Neo4JSimulation(new Neo4jClient(hostUri), context);
    }

    @Override
    protected void initialise(SeedData geoData) {
        Driver nativeDriver = client.unpack();
        initDatabase(nativeDriver);
        initData(nativeDriver, geoData);
    }

    private void initDatabase(Driver nativeDriver) {
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
            add("CREATE CONSTRAINT unique_continent_code ON (continent:Continent) ASSERT continent.code IS UNIQUE");
            add("CREATE CONSTRAINT unique_country_code ON (country:Country) ASSERT country.code IS UNIQUE");
            add("CREATE CONSTRAINT unique_city_code ON (city:City) ASSERT city.code IS UNIQUE");
            add("CREATE CONSTRAINT unique_company_number ON (company:Company) ASSERT company.number IS UNIQUE");
            add("CREATE CONSTRAINT unique_product_id ON (product:Product) ASSERT product.id IS UNIQUE");
            add("CREATE CONSTRAINT unique_purchase_id ON (purchase:Purchase) ASSERT purchase.id IS UNIQUE");
            add("CREATE CONSTRAINT unique_marriage_licence ON (marriage:Marriage) ASSERT marriage.licence IS UNIQUE");
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

    private void initData(Driver nativeDriver, SeedData geoData) {
        LOG.info("Neo4j initialisation of world simulation data started ...");
        Instant start = Instant.now();
        initContinents(nativeDriver, geoData.global());
        LOG.info("Neo4j initialisation of world simulation data ended in: {}", printDuration(start, Instant.now()));
    }

    private void initContinents(Driver nativeDriver, Global global) {
        global.continents().parallelStream().forEach(continent -> {
            try (Session session = nativeDriver.session()) {
                Transaction tx = session.beginTransaction();
                Query interpolatedQuery = new Query(String.format(
                        "CREATE (x:Continent:Region {code: '%s', name: '%s'})", continent.code(), escapeQuotes(continent.name())
                ));
                tx.run(interpolatedQuery);
                tx.commit();
                initCountries(nativeDriver, continent);
            }
        });
    }

    private void initCountries(Driver nativeDriver, Continent continent) {
        continent.countries().parallelStream().forEach(country -> {
            try (Session session = nativeDriver.session()) {
                Transaction tx = session.beginTransaction();
                StringBuilder currencyProps = new StringBuilder();
                if (!country.currencies().isEmpty()) {
                    currencyProps.append(", ");
                    for (int i = 0; i < country.currencies().size(); i++) {
                        Currency currency = country.currencies().get(i);
                        currencyProps.append("currency").append(i + 1).append(": '").append(currency.code()).append("'");
                        if (i + 1 < country.currencies().size()) currencyProps.append(", ");
                    }

                }
                Query query = new Query(String.format(
                        "MATCH (c:Continent {code: '%s'}) CREATE (x:Country:Region {code: '%s', name: '%s'%s})-[:CONTAINED_IN]->(c)",
                        continent.code(), country.code(), escapeQuotes(country.name()), currencyProps
                ));
                tx.run(query);
                tx.commit();
                initCities(session, country);
                initUniversities(session, country);
            }
        });
    }

    private void initCities(Session session, Country country) {
        Transaction tx = session.beginTransaction();
        country.cities().forEach(city -> {
            Query query = new Query(String.format(
                    "MATCH (c:Country {code: '%s'}) CREATE (x:City:Region {code: '%s', name: '%s'})-[:CONTAINED_IN]->(c)",
                    country.code(), city.code(), escapeQuotes(city.name())
            ));
            tx.run(query);
        });
        tx.commit();
    }

    private void initUniversities(Session session, Country country) {
        Transaction tx = session.beginTransaction();
        country.universities().forEach(university -> {
            Query query = new Query(String.format(
                    "MATCH (c:Country {code: '%s'}) CREATE (x:University {name: '%s'})-[:LOCATED_IN]->(c)",
                    country.code(), escapeQuotes(university.name())
            ));
            tx.run(query);
        });
        tx.commit();
    }

    private String escapeQuotes(String string) {
        return string.replace("'", "\\'");
    }

    @Override
    protected PersonAgent<Neo4jTransaction> createPersonAgent(Neo4jClient client, Context context) {
        return new Neo4jPersonAgent(client, context);
    }

    @Override
    protected FriendshipAgent<Neo4jTransaction> createFriendshipAgent(Neo4jClient client, Context context) {
        return new Neo4jFriendshipAgent(client, context);
    }

    @Override
    protected MarriageAgent<Neo4jTransaction> createMarriageAgent(Neo4jClient client, Context context) {
        return new Neo4jMarriageAgent(client, context);
    }

    @Override
    protected ParenthoodAgent<Neo4jTransaction> createParenthoodAgent(Neo4jClient client, Context context) {
        return new Neo4jParenthoodAgent(client, context);
    }

    @Override
    protected LineageAgent<Neo4jTransaction> createLineageAgent(Neo4jClient client, Context context) {
        throw new UnsupportedOperationException("LineageAgent requires reasoning, which is not supported by Neo4j");
    }

    @Override
    protected NationalityAgent<Neo4jTransaction> createNationalityAgent(Neo4jClient client, Context context) {
        throw new UnsupportedOperationException("NationalityAgent requires reasoning, which is not supported by Neo4j");
    }

    @Override
    protected CitizenshipAgent<Neo4jTransaction> createCitizenshipAgent(Neo4jClient client, Context context) {
        throw new UnsupportedOperationException("CitizenshipAgent requires reasoning. Reasoning is not supported by " +
                                                        "Neo4j");
    }

    @Override
    protected MaritalStatusAgent<Neo4jTransaction> createMaritalStatusAgent(Neo4jClient client, Context context) {
        throw new UnsupportedOperationException("MaritalStatusAgent requires reasoning. Reasoning is not supported by" +
                                                        " Neo4j.");
    }

    @Override
    protected CoupleFriendshipAgent<Neo4jTransaction> createCoupleFriendshipAgent(Neo4jClient client,
                                                                                  Context context) {
        throw new UnsupportedOperationException("CoupleFriendshipAgent requires reasoning. Reasoning is not supported" +
                                                        " by Neo4j");
    }

    @Override
    protected GrandparenthoodAgent<Neo4jTransaction> createGrandparenthoodAgent(Neo4jClient client, Context context) {
        throw new UnsupportedOperationException("GrandparenthoodAgent requires reasoning. Reasoning is not supported" +
                                                        " by Neo4j");
    }
}
