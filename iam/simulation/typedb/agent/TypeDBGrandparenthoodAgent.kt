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

import com.vaticle.typedb.driver.api.TypeDBSession
import com.vaticle.typedb.driver.api.TypeDBTransaction
import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.agent.GrandparenthoodAgent
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINED
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINER
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINS
import com.vaticle.typedb.iam.simulation.typedb.Labels.COUNTRY
import com.vaticle.typedb.iam.simulation.typedb.Labels.GRANDCHILD
import com.vaticle.typedb.iam.simulation.typedb.Labels.GRANDPARENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.GRANDPARENTHOOD
import com.vaticle.typedb.iam.simulation.typedb.Labels.PERSON
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENCE
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.benchmark.framework.common.seed.RandomSource
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBSessionEx.readTransaction
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBDriver
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.cVar
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBGrandparenthoodAgent(client: TypeDBDriver, context: Context) :
    GrandparenthoodAgent<TypeDBSession>(client, context) {

    override val actionHandlers = mapOf(
        "doAction" to ::doAction,
    )

    fun doAction(session: TypeDBSession, partition: Country, random: RandomSource): List<Report> {
        session.readTransaction(infer = true).use { tx -> matchGrandparents(tx, partition, context.today()) }
        return emptyList()
    }

    private fun matchGrandparents(tx: TypeDBTransaction, country: Country, birthDate: LocalDateTime) {
        tx.query().get(
            match(
                cVar(PERSON).isa(PERSON).has(BIRTH_DATE, birthDate),
                cVar(COUNTRY).isa(COUNTRY).has(CODE, country.code),
                rel(RESIDENT, cVar(PERSON)).rel(RESIDENCE, cVar(CITY)).isa(RESIDENTSHIP),
                rel(CONTAINED, cVar(CITY)).rel(CONTAINER, cVar(COUNTRY)).isa(CONTAINS),
                rel(GRANDPARENT, cVar(GP)).rel(GRANDCHILD, cVar(PERSON)).isa(GRANDPARENTHOOD)
            ).get()
        ).collect(toList())
    }

    companion object {
        private const val GP = "gp"
    }
}
