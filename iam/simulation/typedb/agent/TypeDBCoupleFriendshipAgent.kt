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
import com.vaticle.typedb.iam.simulation.agent.CoupleFriendshipAgent
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINED
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINER
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINS
import com.vaticle.typedb.iam.simulation.typedb.Labels.COUNTRY
import com.vaticle.typedb.iam.simulation.typedb.Labels.FRIENDSHIP
import com.vaticle.typedb.iam.simulation.typedb.Labels.MARRIAGE
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

class TypeDBCoupleFriendshipAgent(client: TypeDBDriver, context: Context) :
    CoupleFriendshipAgent<TypeDBSession>(client, context) {

    override val actionHandlers = mapOf(
        "doAction" to ::doAction,
    )

    fun doAction(session: TypeDBSession, partition: Country, random: RandomSource): List<Report> {
        // This agent targets the expense of the `put` operation of reasoning. More specifically the cost of `get` to
        // check whether a relation is pre-existing
        session.readTransaction(infer = true).use { tx -> matchFriendships(tx, partition, context.today()) }
        return emptyList()
    }

    private fun matchFriendships(tx: TypeDBTransaction, country: Country, marriageBirthDate: LocalDateTime) {
        tx.query().get(match(
            cVar(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            cVar(X).isa(PERSON).has(BIRTH_DATE, marriageBirthDate),
            rel(RESIDENT, cVar(X)).rel(RESIDENCE, cVar(CITY)).isa(RESIDENTSHIP),
            rel(CONTAINED, cVar(CITY)).rel(CONTAINER, cVar(COUNTRY)).isa(CONTAINS),
            cVar(Y).isa(PERSON),
            rel(cVar(X)).rel(cVar(Y)).isa(FRIENDSHIP),
            rel(cVar(X)).rel(cVar(Y)).isa(MARRIAGE)
        ).get()).collect(toList())
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
    }
}
