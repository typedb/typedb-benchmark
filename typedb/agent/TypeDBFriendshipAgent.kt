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
import com.vaticle.typedb.benchmark.typedb.Labels
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typedb.client.api.answer.ConceptMap
import com.vaticle.typedb.common.collection.Collections
import com.vaticle.typedb.common.collection.Pair
import com.vaticle.typeql.lang.TypeQL
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Collectors
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class TypeDBFriendshipAgent(client: TypeDBClient, context: Context) : FriendshipAgent<TypeDBTransaction>(client, context) {
    override fun matchTeenagers(tx: TypeDBTransaction, country: Country, birthDate: LocalDateTime): Stream<Person> {
        return tx.query().match(
            TypeQL.match(
                TypeQL.`var`(Labels.PERSON).isa(Labels.PERSON).has(Labels.BIRTH_DATE, TypeQL.eq(birthDate)).has(
                    Labels.EMAIL, TypeQL.`var`(Labels.EMAIL)
                ),
                TypeQL.`var`(Labels.COUNTRY).isa(Labels.COUNTRY).has(Labels.CODE, country.code),
                TypeQL.rel(Labels.RESIDENT, TypeQL.`var`(Labels.PERSON))
                    .rel(Labels.RESIDENCE, TypeQL.`var`(Labels.CITY))
                    .isa(Labels.RESIDENTSHIP),
                TypeQL.rel(Labels.CONTAINED, TypeQL.`var`(Labels.CITY))
                    .rel(Labels.CONTAINER, TypeQL.`var`(Labels.COUNTRY))
                    .isa(Labels.CONTAINS)
            )[TypeQL.`var`(Labels.EMAIL)]
        ).map { conceptMap: ConceptMap -> Person(conceptMap[Labels.EMAIL].asAttribute().asString().value) }
    }

    override fun insertFriends(tx: TypeDBTransaction, email1: String, email2: String): Pair<Person, Person>? {
        tx.query().insert(
            TypeQL.match(
                TypeQL.`var`(X).isa(Labels.PERSON).has(Labels.EMAIL, email1),
                TypeQL.`var`(Y).isa(Labels.PERSON).has(Labels.EMAIL, email2)
            ).insert(
                TypeQL.rel(Labels.FRIEND, TypeQL.`var`(X)).rel(Labels.FRIEND, TypeQL.`var`(Y)).isa(Labels.FRIENDSHIP)
            )
        )
        return if (context.isReporting) report(tx, email1, email2) else null
    }

    private fun report(tx: TypeDBTransaction, email1: String, email2: String): Pair<Person, Person> {
        val answers = tx.query().match(
            TypeQL.match(
                TypeQL.`var`(X).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(E1)),
                TypeQL.`var`(E1).eq(email1),
                TypeQL.`var`(Y).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(E2)),
                TypeQL.`var`(E2).eq(email2),
                TypeQL.rel(Labels.FRIEND, TypeQL.`var`(X)).rel(Labels.FRIEND, TypeQL.`var`(Y)).isa(Labels.FRIENDSHIP)
            )[TypeQL.`var`(E1), TypeQL.`var`(E2)]
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
