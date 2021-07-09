/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.benchmark.typedb;

import com.vaticle.typedb.benchmark.common.concept.Continent;
import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Global;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.common.seed.SeedData;
import com.vaticle.typedb.benchmark.simulation.agent.CitizenshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.CoupleFriendshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.LineageAgent;
import com.vaticle.typedb.benchmark.simulation.agent.MaritalStatusAgent;
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent;
import com.vaticle.typedb.benchmark.simulation.agent.NationalityAgent;
import com.vaticle.typedb.benchmark.simulation.agent.ParentshipAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBCitizenshipAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBCoupleFriendshipAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBFriendshipAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBLineageAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBMaritalStatusAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBMarriageAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBNationalityAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBParentshipAgent;
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBPersonAgent;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBSession;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction;
import com.vaticle.typedb.benchmark.simulation.Simulation;
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.PersonAgent;
import com.vaticle.typeql.lang.TypeQL;
import com.vaticle.typeql.lang.pattern.variable.ThingVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import static com.vaticle.typedb.benchmark.common.Util.printDuration;
import static com.vaticle.typedb.benchmark.typedb.Labels.CITY;
import static com.vaticle.typedb.benchmark.typedb.Labels.CODE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTINENT;
import static com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY;
import static com.vaticle.typedb.benchmark.typedb.Labels.CURRENCY;
import static com.vaticle.typedb.benchmark.typedb.Labels.LOCATED;
import static com.vaticle.typedb.benchmark.typedb.Labels.LOCATES;
import static com.vaticle.typedb.benchmark.typedb.Labels.LOCATION;
import static com.vaticle.typedb.benchmark.typedb.Labels.NAME;
import static com.vaticle.typedb.benchmark.typedb.Labels.UNIVERSITY;
import static com.vaticle.typedb.client.api.TypeDBSession.Type.DATA;
import static com.vaticle.typedb.client.api.TypeDBSession.Type.SCHEMA;
import static com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE;
import static com.vaticle.typeql.lang.TypeQL.insert;
import static com.vaticle.typeql.lang.TypeQL.match;
import static com.vaticle.typeql.lang.TypeQL.rel;
import static com.vaticle.typeql.lang.TypeQL.var;

public class TypeDBSimulation extends Simulation<TypeDBClient, TypeDBSession, TypeDBTransaction> {

    private static final Logger LOG = LoggerFactory.getLogger(TypeDBSimulation.class);
    private static final File SCHEMA_FILE = Paths.get("typedb/simulation.gql").toFile();
    private static final String X = "x", Y = "y";

    private TypeDBSimulation(TypeDBClient client, Context context) throws Exception {
        super(client, context);
    }

    public static TypeDBSimulation core(String address, Context context) throws Exception {
        return new TypeDBSimulation(TypeDBClient.core(address, context.databaseName()), context);
    }

    public static TypeDBSimulation cluster(String address, Context context) throws Exception {
        return new TypeDBSimulation(TypeDBClient.cluster(address, context.databaseName()), context);
    }

    @Override
    protected void initialise(SeedData geoData) throws IOException {
        com.vaticle.typedb.client.api.TypeDBClient nativeClient = client.unpack();
        initDatabase(nativeClient);
        initSchema(nativeClient);
        initData(nativeClient, geoData);
    }

    private void initDatabase(com.vaticle.typedb.client.api.TypeDBClient nativeClient) {
        if (nativeClient.databases().contains(context.databaseName())) {
            nativeClient.databases().get(context.databaseName()).delete();
        }
        nativeClient.databases().create(context.databaseName());
    }

    private void initSchema(com.vaticle.typedb.client.api.TypeDBClient nativeClient) throws IOException {
        try (com.vaticle.typedb.client.api.TypeDBSession session = nativeClient.session(context.databaseName(), SCHEMA)) {
            LOG.info("TypeDB initialisation of world simulation schema started ...");
            Instant start = Instant.now();
            String schemaQuery = Files.readString(SCHEMA_FILE.toPath());
            try (com.vaticle.typedb.client.api.TypeDBTransaction tx = session.transaction(WRITE)) {
                tx.query().define(TypeQL.parseQuery(schemaQuery));
                tx.commit();
            }
            LOG.info("TypeDB initialisation of world simulation schema ended in: {}", printDuration(start, Instant.now()));
        }
    }

    private void initData(com.vaticle.typedb.client.api.TypeDBClient nativeClient, SeedData geoData) {
        try (com.vaticle.typedb.client.api.TypeDBSession session = nativeClient.session(context.databaseName(), DATA)) {
            LOG.info("TypeDB initialisation of world simulation data started ...");
            Instant start = Instant.now();
            initContinents(session, geoData.global());
            LOG.info("TypeDB initialisation of world simulation data ended in: {}", printDuration(start, Instant.now()));
        }
    }

    private void initContinents(com.vaticle.typedb.client.api.TypeDBSession session, Global global) {
        global.continents().parallelStream().forEach(continent -> {
            try (com.vaticle.typedb.client.api.TypeDBTransaction tx = session.transaction(WRITE)) {
                tx.query().insert(insert(var().isa(CONTINENT).has(CODE, continent.code()).has(NAME, continent.name())));
                tx.commit();
            }
            initCountries(session, continent);
        });
    }

    private void initCountries(com.vaticle.typedb.client.api.TypeDBSession session, Continent continent) {
        continent.countries().parallelStream().forEach(country -> {
            try (com.vaticle.typedb.client.api.TypeDBTransaction tx = session.transaction(WRITE)) {
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

    private void initCities(com.vaticle.typedb.client.api.TypeDBSession session, Country country) {
        try (com.vaticle.typedb.client.api.TypeDBTransaction tx = session.transaction(WRITE)) {
            country.cities().forEach(city -> tx.query().insert(match(
                    var(X).isa(COUNTRY).has(CODE, country.code())
            ).insert(
                    var(Y).isa(CITY).has(CODE, city.code()).has(NAME, city.name()),
                    rel(CONTAINER, X).rel(CONTAINED, Y).isa(CONTAINS)
            )));
            tx.commit();
        }
    }

    private void initUniversities(com.vaticle.typedb.client.api.TypeDBSession session, Country country) {
        try (com.vaticle.typedb.client.api.TypeDBTransaction tx = session.transaction(WRITE)) {
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
    protected PersonAgent<TypeDBTransaction> createPersonAgent(TypeDBClient client, Context context) {
        return new TypeDBPersonAgent(client, context);
    }

    @Override
    protected FriendshipAgent<TypeDBTransaction> createFriendshipAgent(TypeDBClient client, Context context) {
        return new TypeDBFriendshipAgent(client, context);
    }

    @Override
    protected MarriageAgent<TypeDBTransaction> createMarriageAgent(TypeDBClient client, Context context) {
        return new TypeDBMarriageAgent(client, context);
    }

    @Override
    protected ParentshipAgent<TypeDBTransaction> createParentshipAgent(TypeDBClient client, Context context) {
        return new TypeDBParentshipAgent(client, context);
    }

    @Override
    protected LineageAgent<TypeDBTransaction> createLineageAgent(TypeDBClient client, Context context) {
        return new TypeDBLineageAgent(client, context);
    }

    @Override
    protected NationalityAgent<TypeDBTransaction> createNationalityAgent(TypeDBClient client, Context context) {
        return new TypeDBNationalityAgent(client, context);
    }

    @Override
    protected CitizenshipAgent<TypeDBTransaction> createCitizenshipAgent(TypeDBClient client, Context context) {
        return new TypeDBCitizenshipAgent(client, context);
    }

    @Override
    protected MaritalStatusAgent<TypeDBTransaction> createMaritalStatusAgent(TypeDBClient client, Context context) {
        return new TypeDBMaritalStatusAgent(client, context);
    }

    @Override
    protected CoupleFriendshipAgent<TypeDBTransaction> createCoupleFriendshipAgent(TypeDBClient client,
                                                                                     Context context) {
        return new TypeDBCoupleFriendshipAgent(client, context);
    }
}
