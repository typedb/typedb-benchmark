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
import com.vaticle.typedb.iam.simulation.common.concept.City
import com.vaticle.typedb.iam.simulation.common.concept.Gender
import com.vaticle.typedb.iam.simulation.common.concept.Person
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.agent.PersonAgent
import com.vaticle.typedb.iam.simulation.common.Util.address
import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.typedb.Labels.ADDRESS
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_PLACE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CHILD
import com.vaticle.typedb.iam.simulation.typedb.Labels.CITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.EMAIL
import com.vaticle.typedb.iam.simulation.typedb.Labels.FIRST_NAME
import com.vaticle.typedb.iam.simulation.typedb.Labels.GENDER
import com.vaticle.typedb.iam.simulation.typedb.Labels.LAST_NAME
import com.vaticle.typedb.iam.simulation.typedb.Labels.PERSON
import com.vaticle.typedb.iam.simulation.typedb.Labels.PLACE
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENCE
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.benchmark.framework.common.seed.RandomSource
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBSessionEx.writeTransaction
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBDriver
import com.vaticle.typeql.lang.TypeQL
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.cVar
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBPersonAgent(client: TypeDBDriver, context: Context) : PersonAgent<TypeDBSession>(client, context) {

    override val actionHandlers = mapOf(
        "doAction" to ::doAction,
    )

    fun doAction(session: TypeDBSession, partition: Country, random: RandomSource): List<Report> {
        val reports = mutableListOf<Report>()
        session.writeTransaction().use { tx ->
            for (i in 0 until context.model.populationGrowth) {
                val gender = if (random.nextBoolean()) Gender.MALE else Gender.FEMALE
                val firstName = random.choose(partition.continent.commonFirstNames(gender))
                val lastName = random.choose(partition.continent.commonLastNames)
                val city = random.choose(partition.cities)
                val email = "$firstName.$lastName.${city.code}.${random.nextInt()}@email.com"
                val address = random.address(city)
                val inserted = insertPerson(tx, email, firstName, lastName, address, gender, context.today(), city)
                if (context.isReporting) {
                    requireNotNull(inserted)
                    reports.add(Report(
                        input = listOf(email, firstName, lastName, address, gender, context.today(), city),
                        output = listOf(inserted.first, inserted.second)
                    ))
                } else assert(inserted == null)
            }
            tx.commit()
        }
        return reports
    }

    private fun insertPerson(
        tx: TypeDBTransaction, email: String, firstName: String, lastName: String,
        address: String, gender: Gender, birthDate: LocalDateTime, city: City
    ): Pair<Person, City.Report>? {
        tx.query().insert(
            match(
                cVar(CITY).isa(CITY).has(CODE, city.code)
            ).insert(
                cVar("p").isa(PERSON).has(EMAIL, email).has(FIRST_NAME, firstName)
                    .has(LAST_NAME, lastName).has(ADDRESS, address)
                    .has(GENDER, gender.value).has(BIRTH_DATE, birthDate),
                cVar().rel(PLACE, cVar(CITY)).rel(CHILD, cVar("p")).isa(BIRTH_PLACE),
                cVar().rel(RESIDENCE, cVar(CITY)).rel(RESIDENT, cVar("p")).isa(RESIDENTSHIP)
            )
        )
        return if (context.isReporting) report(tx, email) else null
    }

    private fun report(tx: TypeDBTransaction, email: String): Pair<Person, City.Report> {
        val answers = tx.query().get(match(
            cVar(PERSON).isa(PERSON).has(EMAIL, email)
                .has(FIRST_NAME, cVar(FIRST_NAME))
                .has(LAST_NAME, cVar(LAST_NAME))
                .has(ADDRESS, cVar(ADDRESS))
                .has(GENDER, cVar(GENDER))
                .has(BIRTH_DATE, cVar(BIRTH_DATE)),
            cVar(CITY).has(CODE, cVar(CODE)),
            TypeQL.rel(CHILD, cVar(PERSON)).rel(PLACE, cVar(CITY))
                .isa(BIRTH_PLACE),
            TypeQL.rel(RESIDENT, cVar(PERSON))
                .rel(RESIDENCE, cVar(CITY))
                .isa(RESIDENTSHIP)
        ).get()).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val person = Person(
            email,
            inserted[FIRST_NAME].asAttribute().value.asString(),
            inserted[LAST_NAME].asAttribute().value.asString(),
            inserted[ADDRESS].asAttribute().value.asString(),
            Gender.of(inserted[GENDER].asAttribute().value.asString()),
            inserted[BIRTH_DATE].asAttribute().value.asDateTime()
        )
        val city = City.Report(code = inserted[CODE].asAttribute().value.asString())
        return Pair(person, city)
    }
}
