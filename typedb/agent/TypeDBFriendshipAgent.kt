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
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent
import com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.benchmark.typedb.Labels.CITY
import com.vaticle.typedb.benchmark.typedb.Labels.CODE
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS
import com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY
import com.vaticle.typedb.benchmark.typedb.Labels.EMAIL
import com.vaticle.typedb.benchmark.typedb.Labels.FRIEND
import com.vaticle.typedb.benchmark.typedb.Labels.FRIENDSHIP
import com.vaticle.typedb.benchmark.typedb.Labels.PERSON
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENCE
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENT
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typedb.client.api.answer.ConceptMap
import com.vaticle.typedb.common.collection.Pair
import com.vaticle.typeql.lang.TypeQL.eq
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import java.time.LocalDateTime
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class TypeDBFriendshipAgent(client: TypeDBClient, context: Context) : FriendshipAgent<TypeDBTransaction>(client, context) {
    override fun matchTeenagers(tx: TypeDBTransaction, country: Country, birthDate: LocalDateTime): Stream<Person> {
        return tx.query().match(
            match(
                `var`(PERSON).isa(PERSON).has(BIRTH_DATE, eq(birthDate)).has(EMAIL, `var`(EMAIL)),
                `var`(COUNTRY).isa(COUNTRY).has(CODE, country.code),
                rel(RESIDENT, `var`(PERSON)).rel(RESIDENCE, `var`(CITY)).isa(RESIDENTSHIP),
                rel(CONTAINED, `var`(CITY)).rel(CONTAINER, `var`(COUNTRY)).isa(CONTAINS)
            )[`var`(EMAIL)]
        ).map { conceptMap: ConceptMap -> Person(conceptMap[EMAIL].asAttribute().asString().value) }
    }

    override fun insertFriends(tx: TypeDBTransaction, email1: String, email2: String): Pair<Person, Person>? {
        tx.query().insert(
            match(
                `var`(X).isa(PERSON).has(EMAIL, email1),
                `var`(Y).isa(PERSON).has(EMAIL, email2)
            ).insert(
                rel(FRIEND, `var`(X)).rel(FRIEND, `var`(Y)).isa(FRIENDSHIP)
            )
        )
        return if (context.isReporting) report(tx, email1, email2) else null
    }

    private fun report(tx: TypeDBTransaction, email1: String, email2: String): Pair<Person, Person> {
        val answers = tx.query().match(
            match(
                `var`(X).isa(PERSON).has(EMAIL, `var`(E1)),
                `var`(E1).eq(email1),
                `var`(Y).isa(PERSON).has(EMAIL, `var`(E2)),
                `var`(E2).eq(email2),
                rel(FRIEND, `var`(X)).rel(FRIEND, `var`(Y)).isa(FRIENDSHIP)
            )[`var`(E1), `var`(E2)]
        ).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val person1 = Person(inserted[E1].asAttribute().asString().value)
        val person2 = Person(inserted[E2].asAttribute().asString().value)
        return Pair(person1, person2)
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
        private const val E1 = "e1"
        private const val E2 = "e2"
    }
}
