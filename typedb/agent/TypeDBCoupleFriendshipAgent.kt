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
import com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.benchmark.typedb.Labels.CITY
import com.vaticle.typedb.benchmark.typedb.Labels.CODE
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS
import com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY
import com.vaticle.typedb.benchmark.typedb.Labels.FRIENDSHIP
import com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE
import com.vaticle.typedb.benchmark.typedb.Labels.PERSON
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENCE
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENT
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBCoupleFriendshipAgent(client: TypeDBClient, context: Context) :
    CoupleFriendshipAgent<TypeDBTransaction>(client, context) {
    override fun matchFriendships(tx: TypeDBTransaction, country: Country, marriageBirthDate: LocalDateTime) {
        tx.query().match(match(
            `var`(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            `var`(X).isa(PERSON).has(BIRTH_DATE, marriageBirthDate),
            rel(RESIDENT, `var`(X)).rel(RESIDENCE, `var`(CITY)).isa(RESIDENTSHIP),
            rel(CONTAINED, `var`(CITY)).rel(CONTAINER, `var`(COUNTRY)).isa(CONTAINS),
            `var`(Y).isa(PERSON),
            rel(X).rel(Y).isa(FRIENDSHIP),
            rel(X).rel(Y).isa(MARRIAGE)
        )).collect(toList())
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
    }
}
