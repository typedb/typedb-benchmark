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
package com.vaticle.typedb.benchmark.simulation.agent

import com.vaticle.typedb.benchmark.common.concept.City
import com.vaticle.typedb.benchmark.common.concept.Country
import com.vaticle.typedb.benchmark.common.concept.Gender
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.common.seed.RandomSource
import com.vaticle.typedb.benchmark.simulation.driver.Client
import com.vaticle.typedb.benchmark.simulation.driver.Session
import com.vaticle.typedb.benchmark.simulation.driver.Transaction
import com.vaticle.typedb.common.collection.Pair
import java.time.LocalDateTime

abstract class PersonAgent<TX: Transaction> protected constructor(client: Client<Session<TX>>, context: Context) :
    Agent<Country, TX>(client, context) {
    override val agentClass = PersonAgent::class.java
    override val regions = context.seedData.countries

    override fun run(session: Session<TX>, region: Country, random: RandomSource): List<Report> {
        val reports = mutableListOf<Report>()
        session.writeTransaction().use { tx ->
            for (i in 0 until context.scaleFactor) {
                val gender = if (random.nextBoolean()) Gender.MALE else Gender.FEMALE
                val firstName = random.choose(region.continent.commonFirstNames(gender))
                val lastName = random.choose(region.continent.commonLastNames)
                val city = random.choose(region.cities)
                val email = "$firstName.$lastName.${city.code}.${random.nextInt()}@email.com"
                val address = random.address(city)
                val inserted = insertPerson(tx, email, firstName, lastName, address, gender, context.today(), city)
                if (context.isReporting) {
                    requireNotNull(inserted)
                    reports.add(Report(
                        input = listOf(email, firstName, lastName, address, gender, context.today(), city),
                        output = listOf(inserted.first(), inserted.second())
                    ))
                } else assert(inserted == null)
            }
            tx.commit()
        }
        return reports
    }

    protected abstract fun insertPerson(
        tx: TX, email: String, firstName: String, lastName: String, address: String,
        gender: Gender, birthDate: LocalDateTime, city: City
    ): Pair<Person, City.Report>?
}
