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

import grakn.benchmark.common.concept.Continent;
import grakn.benchmark.common.concept.Country;
import grakn.benchmark.common.concept.Global;
import grakn.benchmark.common.params.Context;
import grakn.benchmark.common.seed.SeedData;
import grakn.benchmark.grakn.agent.GraknFriendshipAgent;
import grakn.benchmark.grakn.agent.GraknPersonAgent;
import grakn.benchmark.grakn.driver.GraknClient;
import grakn.benchmark.grakn.driver.GraknSession;
import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.Simulation;
import grakn.benchmark.simulation.agent.FriendshipAgent;
import grakn.benchmark.simulation.agent.PersonAgent;
import graql.lang.Graql;
import graql.lang.pattern.variable.ThingVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import static grakn.benchmark.common.Util.printDuration;
import static grakn.benchmark.grakn.Labels.CITY;
import static grakn.benchmark.grakn.Labels.CODE;
import static grakn.benchmark.grakn.Labels.CONTAINED;
import static grakn.benchmark.grakn.Labels.CONTAINER;
import static grakn.benchmark.grakn.Labels.CONTAINS;
import static grakn.benchmark.grakn.Labels.CONTINENT;
import static grakn.benchmark.grakn.Labels.COUNTRY;
import static grakn.benchmark.grakn.Labels.CURRENCY;
import static grakn.benchmark.grakn.Labels.LOCATED;
import static grakn.benchmark.grakn.Labels.LOCATES;
import static grakn.benchmark.grakn.Labels.LOCATION;
import static grakn.benchmark.grakn.Labels.NAME;
import static grakn.benchmark.grakn.Labels.UNIVERSITY;
import static grakn.client.api.GraknSession.Type.DATA;
import static grakn.client.api.GraknSession.Type.SCHEMA;
import static grakn.client.api.GraknTransaction.Type.WRITE;
import static graql.lang.Graql.insert;
import static graql.lang.Graql.match;
import static graql.lang.Graql.rel;
import static graql.lang.Graql.var;

public class GraknSimulation extends Simulation<GraknClient, GraknSession, GraknTransaction> {

    private static final Logger LOG = LoggerFactory.getLogger(GraknSimulation.class);
    private static final File SCHEMA_FILE = Paths.get("grakn/simulation.gql").toFile();
    private static final String X = "x", Y = "y";

    private GraknSimulation(GraknClient client, Context context) throws Exception {
        super(client, context);
    }

    public static GraknSimulation core(String address, Context context) throws Exception {
        return new GraknSimulation(GraknClient.core(address, context.databaseName()), context);
    }

    public static GraknSimulation cluster(String address, Context context) throws Exception {
        return new GraknSimulation(GraknClient.cluster(address, context.databaseName()), context);
    }

    @Override
    protected void initialise(SeedData geoData) throws IOException {
        grakn.client.api.GraknClient nativeClient = client.unpack();
        initDatabase(nativeClient);
        initSchema(nativeClient);
        initData(nativeClient, geoData);
    }

    private void initDatabase(grakn.client.api.GraknClient nativeClient) {
        if (nativeClient.databases().contains(context.databaseName())) {
            nativeClient.databases().get(context.databaseName()).delete();
        }
        nativeClient.databases().create(context.databaseName());
    }

    private void initSchema(grakn.client.api.GraknClient nativeClient) throws IOException {
        try (grakn.client.api.GraknSession session = nativeClient.session(context.databaseName(), SCHEMA)) {
            LOG.info("Grakn initialisation of world simulation schema started ...");
            Instant start = Instant.now();
            String schemaQuery = Files.readString(SCHEMA_FILE.toPath());
            try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
                tx.query().define(Graql.parseQuery(schemaQuery));
                tx.commit();
            }
            LOG.info("Grakn initialisation of world simulation schema ended in: {}", printDuration(start, Instant.now()));
        }
    }

    private void initData(grakn.client.api.GraknClient nativeClient, SeedData geoData) {
        try (grakn.client.api.GraknSession session = nativeClient.session(context.databaseName(), DATA)) {
            LOG.info("Grakn initialisation of world simulation data started ...");
            Instant start = Instant.now();
            initContinents(session, geoData.global());
            LOG.info("Grakn initialisation of world simulation data ended in: {}", printDuration(start, Instant.now()));
        }
    }

    private void initContinents(grakn.client.api.GraknSession session, Global global) {
        global.continents().parallelStream().forEach(continent -> {
            try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
                tx.query().insert(insert(var().isa(CONTINENT).has(CODE, continent.code()).has(NAME, continent.name())));
                tx.commit();
            }
            initCountries(session, continent);
        });
    }

    private void initCountries(grakn.client.api.GraknSession session, Continent continent) {
        continent.countries().parallelStream().forEach(country -> {
            try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
                ThingVariable.Thing countryVar = var(Y).isa(COUNTRY).has(CODE, country.code()).has(NAME, country.name());
                country.currencies().forEach(currency -> countryVar.has(CURRENCY, currency.code()));
                tx.query().insert(match(
                        var(X).isa(CONTINENT).has(CODE, continent.code())
                ).insert(
                        countryVar, rel(CONTAINER, X).rel(CONTAINED, Y).isa(CONTAINS)
                ));
                // TODO: Currency should be an entity we relate to by relation
                tx.commit();
            }
            initCities(session, country);
            initUniversities(session, country);
        });
    }

    private void initCities(grakn.client.api.GraknSession session, Country country) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            country.cities().forEach(city -> tx.query().insert(match(
                    var(X).isa(COUNTRY).has(CODE, country.code())
            ).insert(
                    var(Y).isa(CITY).has(CODE, city.code()).has(NAME, city.name()),
                    rel(CONTAINER, X).rel(CONTAINED, Y).isa(CONTAINS)
            )));
            tx.commit();
        }
    }

    private void initUniversities(grakn.client.api.GraknSession session, Country country) {
        try (grakn.client.api.GraknTransaction tx = session.transaction(WRITE)) {
            country.universities().forEach(university -> tx.query().insert(match(
                    var(X).isa(COUNTRY).has(CODE, country.code())
            ).insert(
                    var(Y).isa(UNIVERSITY).has(NAME, university.name()),
                    rel(LOCATION, X).rel(LOCATED, Y).isa(LOCATES)
            )));
            tx.commit();
        }
    }

    @Override
    protected PersonAgent<GraknTransaction> createPersonAgent(GraknClient client, Context context) {
        return new GraknPersonAgent(client, context);
    }

    @Override
    protected FriendshipAgent<GraknTransaction> createFriendshipAgent(GraknClient client, Context context) {
        return new GraknFriendshipAgent(client, context);
    }
}
