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

package com.vaticle.typedb.benchmark.typedb.agent;

import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.simulation.agent.LineageAgent;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction;
import com.vaticle.typeql.lang.TypeQL;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static com.vaticle.typedb.benchmark.typedb.Labels.ANCESTOR;
import static com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE;
import static com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_PLACE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CHILD;
import static com.vaticle.typedb.benchmark.typedb.Labels.CITY;
import static com.vaticle.typedb.benchmark.typedb.Labels.CODE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS;
import static com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY;
import static com.vaticle.typedb.benchmark.typedb.Labels.DESCENDENT;
import static com.vaticle.typedb.benchmark.typedb.Labels.LINEAGE;
import static com.vaticle.typedb.benchmark.typedb.Labels.PERSON;
import static com.vaticle.typedb.benchmark.typedb.Labels.PLACE;
import static com.vaticle.typeql.lang.TypeQL.rel;
import static com.vaticle.typeql.lang.TypeQL.var;

public class TypeDBLineageAgent extends LineageAgent<TypeDBTransaction> {


    protected TypeDBLineageAgent(Client<?, TypeDBTransaction> client, Context context) {
        super(client, context);
    }

    @Override
    protected void matchLineages(TypeDBTransaction tx, Country country, LocalDateTime startDay, LocalDateTime today) {
        tx.query().match(TypeQL.match(
                rel(CONTAINER, COUNTRY).rel(CONTAINED, CITY).isa(CONTAINS),
                var(COUNTRY).isa(COUNTRY).has(CODE, country.code()),
                var(CITY).isa(CITY),
                var(ANCESTOR).isa(PERSON).has(BIRTH_DATE, startDay),
                var().rel(PLACE, var(CITY)).rel(CHILD, var(ANCESTOR)).isa(BIRTH_PLACE),
                var(DESCENDENT).isa(PERSON).has(BIRTH_DATE, today),
                rel(ANCESTOR, ANCESTOR).rel(DESCENDENT, DESCENDENT).isa(LINEAGE)
        )).collect(Collectors.toList());
    }
}
