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
package com.vaticle.typedb.benchmark.typedb.agent

import com.vaticle.typedb.benchmark.common.concept.Country
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.simulation.agent.NationalityAgent
import com.vaticle.typedb.benchmark.simulation.driver.Client
import com.vaticle.typedb.benchmark.typedb.Labels
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typeql.lang.TypeQL
import java.time.LocalDateTime
import java.util.stream.Collectors
import java.util.stream.Collectors.toList

class TypeDBNationalityAgent(client: TypeDBClient, context: Context) :
    NationalityAgent<TypeDBTransaction>(client, context) {
    override fun matchNationalities(tx: TypeDBTransaction, country: Country, today: LocalDateTime) {
        tx.query().match(
            TypeQL.match(
                TypeQL.`var`(Labels.COUNTRY).isa(Labels.COUNTRY).has(Labels.CODE, country.code),
                TypeQL.`var`(Labels.NATIONAL).isa(Labels.PERSON).has(Labels.BIRTH_DATE, today),
                TypeQL.rel(Labels.NATIONAL, Labels.NATIONAL).rel(Labels.NATION, Labels.COUNTRY).isa(Labels.NATIONALITY)
            )
        ).collect(toList())
    }
}
