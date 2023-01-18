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

import com.vaticle.typedb.benchmark.common.concept.City
import com.vaticle.typedb.benchmark.common.concept.Gender
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.simulation.agent.PersonAgent
import com.vaticle.typedb.benchmark.typedb.Labels
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typedb.common.collection.Collections
import com.vaticle.typedb.common.collection.Pair
import com.vaticle.typeql.lang.TypeQL
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Collectors
import java.util.stream.Collectors.toList

class TypeDBPersonAgent(client: TypeDBClient, context: Context) : PersonAgent<TypeDBTransaction>(client, context) {
    override fun insertPerson(
        tx: TypeDBTransaction, email: String, firstName: String, lastName: String,
        address: String, gender: Gender, birthDate: LocalDateTime, city: City
    ): Pair<Person, City>? {
        tx.query().insert(
            TypeQL.match(
                TypeQL.`var`(Labels.CITY).isa(Labels.CITY).has(Labels.CODE, city.code)
            ).insert(
                TypeQL.`var`("p").isa(Labels.PERSON).has(Labels.EMAIL, email).has(Labels.FIRST_NAME, firstName)
                    .has(Labels.LAST_NAME, lastName)
                    .has(Labels.ADDRESS, address).has(Labels.GENDER, gender.value)
                    .has(Labels.BIRTH_DATE, birthDate),
                TypeQL.`var`().rel(Labels.PLACE, TypeQL.`var`(Labels.CITY)).rel(Labels.CHILD, TypeQL.`var`("p"))
                    .isa(Labels.BIRTH_PLACE),
                TypeQL.`var`().rel(Labels.RESIDENCE, TypeQL.`var`(Labels.CITY)).rel(Labels.RESIDENT, TypeQL.`var`("p"))
                    .isa(Labels.RESIDENTSHIP)
            )
        )
        return if (context.isReporting) report(tx, email) else null
    }

    private fun report(tx: TypeDBTransaction, email: String): Pair<Person, City> {
        val answers = tx.query().match(
            TypeQL.match(
                TypeQL.`var`(Labels.PERSON).isa(Labels.PERSON).has(Labels.EMAIL, email)
                    .has(Labels.FIRST_NAME, TypeQL.`var`(Labels.FIRST_NAME))
                    .has(Labels.LAST_NAME, TypeQL.`var`(Labels.LAST_NAME))
                    .has(Labels.ADDRESS, TypeQL.`var`(Labels.ADDRESS))
                    .has(Labels.GENDER, TypeQL.`var`(Labels.GENDER))
                    .has(Labels.BIRTH_DATE, TypeQL.`var`(Labels.BIRTH_DATE)),
                TypeQL.`var`(Labels.CITY).has(Labels.CODE, TypeQL.`var`(Labels.CODE)),
                TypeQL.rel(Labels.CHILD, TypeQL.`var`(Labels.PERSON)).rel(Labels.PLACE, TypeQL.`var`(Labels.CITY))
                    .isa(Labels.BIRTH_PLACE),
                TypeQL.rel(Labels.RESIDENT, TypeQL.`var`(Labels.PERSON))
                    .rel(Labels.RESIDENCE, TypeQL.`var`(Labels.CITY))
                    .isa(Labels.RESIDENTSHIP)
            )
        ).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val person = Person(
            email,
            inserted[Labels.FIRST_NAME].asAttribute().asString().value,
            inserted[Labels.LAST_NAME].asAttribute().asString().value,
            inserted[Labels.ADDRESS].asAttribute().asString().value,
            Gender.of(inserted[Labels.GENDER].asAttribute().asString().value),
            inserted[Labels.BIRTH_DATE].asAttribute().asDateTime().value
        )
        val city = City(inserted[Labels.CODE].asAttribute().asString().value)
        return Pair(person, city)
    }
}
