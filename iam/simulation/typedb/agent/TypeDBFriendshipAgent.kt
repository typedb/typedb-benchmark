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
import com.vaticle.typedb.iam.simulation.common.concept.Person
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.agent.FriendshipAgent
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINED
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINER
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINS
import com.vaticle.typedb.iam.simulation.typedb.Labels.COUNTRY
import com.vaticle.typedb.iam.simulation.typedb.Labels.EMAIL
import com.vaticle.typedb.iam.simulation.typedb.Labels.FRIEND
import com.vaticle.typedb.iam.simulation.typedb.Labels.FRIENDSHIP
import com.vaticle.typedb.iam.simulation.typedb.Labels.PERSON
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENCE
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.driver.api.answer.ConceptMap
import com.vaticle.typedb.benchmark.framework.common.seed.RandomSource
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBSessionEx.writeTransaction
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBDriver
import com.vaticle.typeql.lang.TypeQL.eq
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.cVar
import java.time.LocalDateTime
import java.util.Comparator
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class TypeDBFriendshipAgent(client: TypeDBDriver, context: Context) : FriendshipAgent<TypeDBSession>(client, context) {

    override val actionHandlers = mapOf(
        "doAction" to ::doAction,
    )

    fun doAction(session: TypeDBSession, partition: Country, random: RandomSource): List<Report> {
        val reports = mutableListOf<Report>()
        session.writeTransaction().use { tx ->
            val birthDate = context.today().minusYears(context.model.ageOfFriendship.toLong())
            val teenagers = matchTeenagers(tx, partition, birthDate).sorted(Comparator.comparing { it.email }).collect(toList())
            random.randomPairs(teenagers, log2(context.model.populationGrowth).coerceAtMost(1)).forEach { friends ->
                val inserted = insertFriends(tx, friends.first.email, friends.second.email)
                if (context.isReporting) {
                    requireNotNull(inserted)
                    reports.add(Report(
                        input = listOf(friends.first.email, friends.second.email),
                        output = listOf(inserted.first, inserted.second)
                    ))
                } else assert(inserted == null)
            }
            tx.commit()
        }
        return reports
    }

    private fun matchTeenagers(tx: TypeDBTransaction, country: Country, birthDate: LocalDateTime): Stream<Person> {
        return tx.query().get(
            match(
                cVar(PERSON).isa(PERSON).has(BIRTH_DATE, eq(birthDate)).has(EMAIL, cVar(EMAIL)),
                cVar(COUNTRY).isa(COUNTRY).has(CODE, country.code),
                rel(RESIDENT, cVar(PERSON)).rel(RESIDENCE, cVar(CITY)).isa(RESIDENTSHIP),
                rel(CONTAINED, cVar(CITY)).rel(CONTAINER, cVar(COUNTRY)).isa(CONTAINS)
            )[cVar(EMAIL)]
        ).map { conceptMap: ConceptMap -> Person(email = conceptMap[EMAIL].asAttribute().value.asString()) }
    }

    private fun insertFriends(tx: TypeDBTransaction, email1: String, email2: String): Pair<Person, Person>? {
        tx.query().insert(
            match(
                cVar(X).isa(PERSON).has(EMAIL, email1),
                cVar(Y).isa(PERSON).has(EMAIL, email2)
            ).insert(
                rel(FRIEND, cVar(X)).rel(FRIEND, cVar(Y)).isa(FRIENDSHIP)
            )
        )
        return if (context.isReporting) report(tx, email1, email2) else null
    }

    private fun report(tx: TypeDBTransaction, email1: String, email2: String): Pair<Person, Person> {
        val answers = tx.query().get(
            match(
                cVar(X).isa(PERSON).has(EMAIL, cVar(E1)),
                cVar(E1).eq(email1),
                cVar(Y).isa(PERSON).has(EMAIL, cVar(E2)),
                cVar(E2).eq(email2),
                rel(FRIEND, cVar(X)).rel(FRIEND, cVar(Y)).isa(FRIENDSHIP)
            )[cVar(E1), cVar(E2)]
        ).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val person1 = Person(email = inserted[E1].asAttribute().value.asString())
        val person2 = Person(email = inserted[E2].asAttribute().value.asString())
        return Pair(person1, person2)
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
        private const val E1 = "e1"
        private const val E2 = "e2"
    }
}
