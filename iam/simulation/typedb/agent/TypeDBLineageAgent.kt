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
import com.vaticle.typedb.iam.simulation.agent.LineageAgent
import com.vaticle.typedb.iam.simulation.typedb.Labels.ANCESTOR
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_PLACE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CHILD
import com.vaticle.typedb.iam.simulation.typedb.Labels.CITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINED
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINER
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINS
import com.vaticle.typedb.iam.simulation.typedb.Labels.COUNTRY
import com.vaticle.typedb.iam.simulation.typedb.Labels.DESCENDENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.LINEAGE
import com.vaticle.typedb.iam.simulation.typedb.Labels.PERSON
import com.vaticle.typedb.iam.simulation.typedb.Labels.PLACE
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typedb.simulation.typedb.TypeDBSessionEx.readTransaction
import com.vaticle.typedb.simulation.typedb.TypeDBClient
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBLineageAgent(client: TypeDBClient, context: Context) : LineageAgent<TypeDBSession>(client, context) {

    override fun run(session: TypeDBSession, partition: Country, random: RandomSource): List<Report> {
        session.readTransaction(infer = true).use { tx -> matchLineages(tx, partition, context.startDay(), context.today()) }
        return emptyList()
    }

    private fun matchLineages(tx: TypeDBTransaction, country: Country, startDay: LocalDateTime, today: LocalDateTime) {
        tx.query().match(match(
            rel(CONTAINER, COUNTRY).rel(CONTAINED, CITY).isa(CONTAINS),
            `var`(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            `var`(CITY).isa(CITY),
            `var`(ANCESTOR).isa(PERSON).has(BIRTH_DATE, startDay),
            `var`().rel(PLACE, `var`(CITY)).rel(CHILD, `var`(ANCESTOR)).isa(BIRTH_PLACE),
            `var`(DESCENDENT).isa(PERSON).has(BIRTH_DATE, today),
            rel(ANCESTOR, ANCESTOR).rel(DESCENDENT, DESCENDENT).isa(LINEAGE)
        )).collect(toList())
    }
}
