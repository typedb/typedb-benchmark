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
import com.vaticle.typedb.benchmark.typedb.Labels.ADDRESS
import com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_PLACE
import com.vaticle.typedb.benchmark.typedb.Labels.CHILD
import com.vaticle.typedb.benchmark.typedb.Labels.CITY
import com.vaticle.typedb.benchmark.typedb.Labels.CODE
import com.vaticle.typedb.benchmark.typedb.Labels.EMAIL
import com.vaticle.typedb.benchmark.typedb.Labels.FIRST_NAME
import com.vaticle.typedb.benchmark.typedb.Labels.GENDER
import com.vaticle.typedb.benchmark.typedb.Labels.LAST_NAME
import com.vaticle.typedb.benchmark.typedb.Labels.PERSON
import com.vaticle.typedb.benchmark.typedb.Labels.PLACE
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENCE
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENT
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typedb.common.collection.Pair
import com.vaticle.typeql.lang.TypeQL
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.`var`
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBPersonAgent(client: TypeDBClient, context: Context) : PersonAgent<TypeDBTransaction>(client, context) {
    override fun insertPerson(
        tx: TypeDBTransaction, email: String, firstName: String, lastName: String,
        address: String, gender: Gender, birthDate: LocalDateTime, city: City
    ): Pair<Person, City.Report>? {
        tx.query().insert(
            match(
                `var`(CITY).isa(CITY).has(CODE, city.code)
            ).insert(
                `var`("p").isa(PERSON).has(EMAIL, email).has(FIRST_NAME, firstName)
                    .has(LAST_NAME, lastName).has(ADDRESS, address)
                    .has(GENDER, gender.value).has(BIRTH_DATE, birthDate),
                `var`().rel(PLACE, `var`(CITY)).rel(CHILD, `var`("p")).isa(BIRTH_PLACE),
                `var`().rel(RESIDENCE, `var`(CITY)).rel(RESIDENT, `var`("p")).isa(RESIDENTSHIP)
            )
        )
        return if (context.isReporting) report(tx, email) else null
    }

    private fun report(tx: TypeDBTransaction, email: String): Pair<Person, City.Report> {
        val answers = tx.query().match(match(
            `var`(PERSON).isa(PERSON).has(EMAIL, email)
                .has(FIRST_NAME, `var`(FIRST_NAME))
                .has(LAST_NAME, `var`(LAST_NAME))
                .has(ADDRESS, `var`(ADDRESS))
                .has(GENDER, `var`(GENDER))
                .has(BIRTH_DATE, `var`(BIRTH_DATE)),
            `var`(CITY).has(CODE, `var`(CODE)),
            TypeQL.rel(CHILD, `var`(PERSON)).rel(PLACE, `var`(CITY))
                .isa(BIRTH_PLACE),
            TypeQL.rel(RESIDENT, `var`(PERSON))
                .rel(RESIDENCE, `var`(CITY))
                .isa(RESIDENTSHIP)
        )).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val person = Person(
            email,
            inserted[FIRST_NAME].asAttribute().asString().value,
            inserted[LAST_NAME].asAttribute().asString().value,
            inserted[ADDRESS].asAttribute().asString().value,
            Gender.of(inserted[GENDER].asAttribute().asString().value),
            inserted[BIRTH_DATE].asAttribute().asDateTime().value
        )
        val city = City.Report(code = inserted[CODE].asAttribute().asString().value)
        return Pair(person, city)
    }
}
