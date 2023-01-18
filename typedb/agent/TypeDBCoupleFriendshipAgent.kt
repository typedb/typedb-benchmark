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
import com.vaticle.typedb.benchmark.simulation.agent.CoupleFriendshipAgent
import com.vaticle.typedb.benchmark.typedb.Labels
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typeql.lang.TypeQL
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBCoupleFriendshipAgent(client: TypeDBClient, context: Context) :
    CoupleFriendshipAgent<TypeDBTransaction>(client, context) {
    override fun matchFriendships(tx: TypeDBTransaction, country: Country, marriageBirthDate: LocalDateTime) {
        tx.query().match(TypeQL.match(
            TypeQL.`var`(Labels.COUNTRY).isa(Labels.COUNTRY).has(Labels.CODE, country.code),
            TypeQL.`var`(X).isa(Labels.PERSON).has(Labels.BIRTH_DATE, marriageBirthDate),
            TypeQL.rel(Labels.RESIDENT, TypeQL.`var`(X)).rel(Labels.RESIDENCE, TypeQL.`var`(Labels.CITY))
                .isa(Labels.RESIDENTSHIP),
            TypeQL.rel(Labels.CONTAINED, TypeQL.`var`(Labels.CITY))
                .rel(Labels.CONTAINER, TypeQL.`var`(Labels.COUNTRY))
                .isa(Labels.CONTAINS),
            TypeQL.`var`(Y).isa(Labels.PERSON),
            TypeQL.rel(X).rel(Y).isa(Labels.FRIENDSHIP),
            TypeQL.rel(X).rel(Y).isa(Labels.MARRIAGE)
        )).collect(toList())
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
    }
}
