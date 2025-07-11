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
package com.vaticle.typedb.iam.simulation.typedb.agent

import com.vaticle.typedb.client.api.TypeDBSession
import com.vaticle.typedb.client.api.TypeDBTransaction
import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.agent.NationalityAgent
import com.vaticle.typedb.iam.simulation.typedb.Labels
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.COUNTRY
import com.vaticle.typedb.iam.simulation.typedb.Labels.NATIONAL
import com.vaticle.typedb.iam.simulation.typedb.Labels.NATIONALITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.PERSON
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typedb.simulation.typedb.TypeDBSessionEx.readTransaction
import com.vaticle.typedb.simulation.typedb.TypeDBClient
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBNationalityAgent(client: TypeDBClient, context: Context) :
    NationalityAgent<TypeDBSession>(client, context) {

    override fun run(session: TypeDBSession, partition: Country, random: RandomSource): List<Report> {
        if (context.isReporting) throw RuntimeException("Reports are not comparable for reasoning agents.")
        session.readTransaction(infer = true).use { tx -> matchNationalities(tx, partition, context.today()) }
        return emptyList()
    }

    private fun matchNationalities(tx: TypeDBTransaction, country: Country, today: LocalDateTime) {
        tx.query().match(match(
            `var`(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            `var`(NATIONAL).isa(PERSON).has(BIRTH_DATE, today),
            rel(NATIONAL, NATIONAL).rel(Labels.NATION, COUNTRY).isa(NATIONALITY)
        )).collect(toList())
    }
}
