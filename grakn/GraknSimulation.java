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

import grakn.benchmark.common.params.Context;
import grakn.benchmark.common.seed.GeoData;
import grakn.benchmark.grakn.agent.GraknPersonAgent;
import grakn.benchmark.grakn.driver.GraknClient;
import grakn.benchmark.grakn.driver.GraknSession;
import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.Simulation;
import grakn.benchmark.simulation.agent.PersonAgent;
import graql.lang.Graql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import static grakn.benchmark.common.Util.printDuration;
import static grakn.benchmark.grakn.agent.Label.CITY;
import static grakn.benchmark.grakn.agent.Label.CONTINENT;
import static grakn.benchmark.grakn.agent.Label.COUNTRY;
import static grakn.benchmark.grakn.agent.Label.CURRENCY;
import static grakn.benchmark.grakn.agent.Label.CURRENCY_CODE;
import static grakn.benchmark.grakn.agent.Label.LOCATION_HIERARCHY;
import static grakn.benchmark.grakn.agent.Label.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Label.SUBORDINATE;
import static grakn.benchmark.grakn.agent.Label.SUPERIOR;
import static grakn.client.api.GraknSession.Type.DATA;
import static grakn.client.api.GraknSession.Type.SCHEMA;
import static grakn.client.api.GraknTransaction.Type.WRITE;
import static graql.lang.Graql.rel;
import static graql.lang.Graql.var;

public class GraknSimulation extends Simulation<GraknClient, GraknSession, GraknTransaction> {

    private static final Logger LOG = LoggerFactory.getLogger(GraknSimulation.class);
    private static final File SCHEMA_FILE = Paths.get("grakn/simulation.gql").toFile();
    private static final String DATABASE_NAME = "simulation";

    private final grakn.client.api.GraknClient nativeClient;

    private GraknSimulation(GraknClient client, Context context) throws Exception {
        super(client, context);
        this.nativeClient = client.unpack();
    }

    public static GraknSimulation core(String address, Context context) throws Exception {
        return new GraknSimulation(GraknClient.core(address, DATABASE_NAME), context);
    }

    public static GraknSimulation cluster(String address, Context context) throws Exception {
        return new GraknSimulation(GraknClient.cluster(address, DATABASE_NAME), context);
    }

    @Override
    protected void initialise(GeoData geoData) throws IOException {
        initDatabase();
        initSchema();
        initData(geoData);
    }

    private void initDatabase() {
        if (nativeClient.databases().contains(DATABASE_NAME)) nativeClient.databases().get(DATABASE_NAME).delete();
        nativeClient.databases().create(DATABASE_NAME);
    }

    private void initSchema() throws IOException {
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

    private void initData(GeoData geoData) {
        try (grakn.client.api.GraknSession session = nativeClient.session(DATABASE_NAME, DATA)) {
            LOG.info("Grakn initialisation of world simulation data started ...");
            Instant start = Instant.now();
            initCurrencies(session, geoData.currencies());
            initContinents(session, geoData.continents());
            initCountries(session, geoData.countries());
            initCities(session, geoData.cities());
            LOG.info("Grakn initialisation of world simulation data ended in {}", printDuration(start, Instant.now()));
        }
    }

    private void initCurrencies(grakn.client.api.GraknSession session, List<GeoData.Currency> currencies) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            // TODO: Currency should be an entity, and 'code' should be renamed to 'symbol'
            currencies.forEach(currency -> tx.query().insert(Graql.insert(
                    var().eq(currency.name()).isa(CURRENCY).has(CURRENCY_CODE, currency.symbol())
            )));
            tx.commit();
        }
    }

    private void initContinents(grakn.client.api.GraknSession session, List<GeoData.Continent> continents) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            continents.forEach(continent -> tx.query().insert(Graql.insert(
                    var().isa(CONTINENT).has(LOCATION_NAME, continent.name())
            )));
            tx.commit();
        }
    }

    private void initCountries(grakn.client.api.GraknSession session, List<GeoData.Country> countries) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            // TODO: Currency should be an entity we relate to by relation
            countries.forEach(country -> {
                tx.query().insert(Graql.match(
                        var("x").isa(CONTINENT).has(LOCATION_NAME, country.continent().name())
                ).insert(
                        var("y").isa(COUNTRY)
                                .has(LOCATION_NAME, country.name())
                                .has(CURRENCY, country.currency().name()),
                        rel(SUPERIOR, "x").rel(SUBORDINATE, "y").isa(LOCATION_HIERARCHY)
                ));
            });
            tx.commit();
        }
    }

    private void initCities(grakn.client.api.GraknSession session, List<GeoData.City> cities) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            cities.forEach(city -> tx.query().insert(Graql.match(
                    var("x").isa(COUNTRY).has(LOCATION_NAME, city.country().name())
            ).insert(
                    var("y").isa(CITY).has(LOCATION_NAME, city.name()),
                    rel(SUPERIOR, "x").rel(SUBORDINATE, "y").isa(LOCATION_HIERARCHY)
            )));
            tx.commit();
        }
    }

    @Override
    protected PersonAgent<GraknTransaction> createPersonAgent(GraknClient client, Context context) {
        return new GraknPersonAgent(client, context);
    }
}
