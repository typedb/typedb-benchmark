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

package grakn.benchmark.grakn;

import grakn.benchmark.common.Config;
import grakn.benchmark.grakn.agent.GraknAgeUpdateAgent;
import grakn.benchmark.grakn.agent.GraknArbitraryOneHopAgent;
import grakn.benchmark.grakn.agent.GraknCompanyAgent;
import grakn.benchmark.grakn.agent.GraknEmploymentAgent;
import grakn.benchmark.grakn.agent.GraknFindCurrentResidentsAgent;
import grakn.benchmark.grakn.agent.GraknFindLivedInAgent;
import grakn.benchmark.grakn.agent.GraknFindSpecificMarriageAgent;
import grakn.benchmark.grakn.agent.GraknFindSpecificPersonAgent;
import grakn.benchmark.grakn.agent.GraknFindTransactionCurrencyAgent;
import grakn.benchmark.grakn.agent.GraknFourHopAgent;
import grakn.benchmark.grakn.agent.GraknFriendshipAgent;
import grakn.benchmark.grakn.agent.GraknMarriageAgent;
import grakn.benchmark.grakn.agent.GraknMeanWageAgent;
import grakn.benchmark.grakn.agent.GraknParentshipAgent;
import grakn.benchmark.grakn.agent.GraknPersonBirthAgent;
import grakn.benchmark.grakn.agent.GraknProductAgent;
import grakn.benchmark.grakn.agent.GraknPurchaseAgent;
import grakn.benchmark.grakn.agent.GraknRelocationAgent;
import grakn.benchmark.grakn.agent.GraknThreeHopAgent;
import grakn.benchmark.grakn.agent.GraknTwoHopAgent;
import grakn.benchmark.grakn.driver.GraknClient;
import grakn.benchmark.grakn.driver.GraknSession;
import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.Simulation;
import grakn.benchmark.simulation.agent.Agent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import graql.lang.Graql;
import graql.lang.pattern.variable.ThingVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import static grakn.benchmark.common.Util.printDuration;
import static grakn.benchmark.grakn.agent.Types.CONTINENT;
import static grakn.benchmark.grakn.agent.Types.COUNTRY;
import static grakn.benchmark.grakn.agent.Types.CURRENCY;
import static grakn.benchmark.grakn.agent.Types.CURRENCY_CODE;
import static grakn.benchmark.grakn.agent.Types.LANGUAGE;
import static grakn.benchmark.grakn.agent.Types.LOCATION_HIERARCHY;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.SUBORDINATE;
import static grakn.benchmark.grakn.agent.Types.SUPERIOR;
import static grakn.client.api.GraknSession.Type.DATA;
import static grakn.client.api.GraknSession.Type.SCHEMA;
import static grakn.client.api.GraknTransaction.Type.WRITE;
import static graql.lang.Graql.rel;
import static graql.lang.Graql.var;

public class GraknSimulation extends Simulation<GraknClient, GraknSession, GraknTransaction> {

    private static final Logger LOG = LoggerFactory.getLogger(GraknSimulation.class);
    private static final File SCHEMA_FILE = Paths.get("grakn/simulation.gql").toFile();
    private static final String DATABASE_NAME = "simulation";

    private GraknSimulation(GraknClient client, int randomSeed, List<Config.Agent> agentConfigs,
                            SimulationContext context) throws Exception {
        super(client, randomSeed, agentConfigs, context);
    }

    public static GraknSimulation core(String hostUri, int randomSeed, List<Config.Agent> agentConfigs,
                                       SimulationContext context) throws Exception {
        return new GraknSimulation(GraknClient.core(hostUri, DATABASE_NAME), randomSeed, agentConfigs, context);
    }

    public static GraknSimulation cluster(String hostUri, int randomSeed, List<Config.Agent> agentConfigs,
                                          SimulationContext context) throws Exception {
        return new GraknSimulation(GraknClient.cluster(hostUri, DATABASE_NAME), randomSeed, agentConfigs, context);
    }

    @Override
    protected void initialiseDatabase() throws IOException {
        grakn.client.api.GraknClient nativeClient = client().unpack();
        initialiseDatabase(nativeClient);
        initialiseSchema(nativeClient);
    }

    private void initialiseDatabase(grakn.client.api.GraknClient nativeClient) {
        if (nativeClient.databases().contains(DATABASE_NAME)) nativeClient.databases().get(DATABASE_NAME).delete();
        nativeClient.databases().create(DATABASE_NAME);
    }

    private void initialiseSchema(grakn.client.api.GraknClient nativeClient) throws IOException {
        try (grakn.client.api.GraknSession session = nativeClient.session(DATABASE_NAME, SCHEMA)) {
            LOG.info("Grakn initialisation of world simulation schema started ...");
            Instant start = Instant.now();
            String schemaQuery = Files.readString(SCHEMA_FILE.toPath());
            try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
                tx.query().define(Graql.parseQuery(schemaQuery));
                tx.commit();
            }
            LOG.info("Grakn initialisation of world simulation schema ended in {}", printDuration(start, Instant.now()));
        }
    }

    @Override
    protected void initialiseData(GeoData geoData) {
        grakn.client.api.GraknClient nativeClient = client().unpack();
        try (grakn.client.api.GraknSession session = nativeClient.session(DATABASE_NAME, DATA)) {
            LOG.info("Grakn initialisation of world simulation data started ...");
            Instant start = Instant.now();
            initialiseCurrencies(session, geoData.currencies());
            initialiseContinents(session, geoData.continents());
            initialiseCountries(session, geoData.countries());
            initialiseCities(session, geoData.cities());
            LOG.info("Grakn initialisation of world simulation data ended in {}", printDuration(start, Instant.now()));
        }
    }

    private void initialiseCurrencies(grakn.client.api.GraknSession session, List<GeoData.Currency> currencies) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            // TODO: Currency should be an entity, and 'code' should be renamed to 'symbol'
            currencies.forEach(currency -> tx.query().insert(Graql.insert(
                    var().eq(currency.name()).isa(CURRENCY).has(CURRENCY_CODE, currency.symbol())
            )));
            tx.commit();
        }
    }

    private void initialiseContinents(grakn.client.api.GraknSession session, List<GeoData.Continent> continents) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            continents.forEach(continent -> tx.query().insert(Graql.insert(
                    var().isa(CONTINENT).has(LOCATION_NAME, continent.name())
            )));
            tx.commit();
        }
    }

    private void initialiseCountries(grakn.client.api.GraknSession session, List<GeoData.Country> countries) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            // TODO: Currency should be an entity we relate to by relation
            countries.forEach(country -> {
                ThingVariable.Thing countryVar = var("y").isa(COUNTRY)
                        .has(LOCATION_NAME, country.name())
                        .has(CURRENCY, country.currency().name());
                for (String language : country.languages()) countryVar = countryVar.has(LANGUAGE, language);
                tx.query().insert(Graql.match(
                        var("x").isa(CONTINENT).has(LOCATION_NAME, country.continent().name())
                ).insert(
                        countryVar, rel(SUPERIOR, "x").rel(SUBORDINATE, "y").isa(LOCATION_HIERARCHY)
                ));
            });
            tx.commit();
        }
    }

    private void initialiseCities(grakn.client.api.GraknSession session, List<GeoData.City> cities) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            cities.forEach(city -> tx.query().insert(Graql.match(
                    var("x").isa(COUNTRY).has(LOCATION_NAME, city.country().name())
            ).insert(
                    var("y").isa("city").has(LOCATION_NAME, city.name()),
                    rel(SUPERIOR, "x").rel(SUBORDINATE, "y").isa(LOCATION_HIERARCHY)
            )));
            tx.commit();
        }
    }

    @Override
    protected Agent<?, GraknTransaction> createAgeUpdateAgent() {
        return new GraknAgeUpdateAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createArbitraryOneHopAgent() {
        return new GraknArbitraryOneHopAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createCompanyAgent() {
        return new GraknCompanyAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createEmploymentAgent() {
        return new GraknEmploymentAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createFindCurrentResidentsAgent() {
        return new GraknFindCurrentResidentsAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createFindLivedInAgent() {
        return new GraknFindLivedInAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createFindSpecificMarriageAgent() {
        return new GraknFindSpecificMarriageAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createFindSpecificPersonAgent() {
        return new GraknFindSpecificPersonAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createFindTransactionCurrencyAgent() {
        return new GraknFindTransactionCurrencyAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createFourHopAgent() {
        return new GraknFourHopAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createFriendshipAgent() {
        return new GraknFriendshipAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createMarriageAgent() {
        return new GraknMarriageAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createMeanWageAgent() {
        return new GraknMeanWageAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createParentshipAgent() {
        return new GraknParentshipAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createPersonBirthAgent() {
        return new GraknPersonBirthAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createProductAgent() {
        return new GraknProductAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createPurchaseAgent() {
        return new GraknPurchaseAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createRelocationAgent() {
        return new GraknRelocationAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createThreeHopAgent() {
        return new GraknThreeHopAgent(client(), context());
    }

    @Override
    protected Agent<?, GraknTransaction> createTwoHopAgent() {
        return new GraknTwoHopAgent(client(), context());
    }
}
