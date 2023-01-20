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

import com.vaticle.typedb.benchmark.common.concept.Country
import com.vaticle.typedb.benchmark.common.concept.Gender
import com.vaticle.typedb.benchmark.common.concept.Gender.FEMALE
import com.vaticle.typedb.benchmark.common.concept.Gender.MALE
import com.vaticle.typedb.benchmark.common.concept.Marriage
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.common.seed.RandomSource
import com.vaticle.typedb.benchmark.simulation.driver.Client
import com.vaticle.typedb.benchmark.simulation.driver.Session
import com.vaticle.typedb.benchmark.simulation.driver.Transaction
import com.vaticle.typedb.common.collection.Pair
import java.time.LocalDateTime
import java.util.Comparator.comparing
import java.util.stream.Collectors.toList
import java.util.stream.Stream

abstract class MarriageAgent<TX: Transaction> protected constructor(client: Client<Session<TX>>, context: Context) :
    Agent<Country, TX>(client, context) {
    override val agentClass = MarriageAgent::class.java
    override val regions = context.seedData.countries

    override fun run(session: Session<TX>, region: Country, random: RandomSource): List<Report> {
        val reports = mutableListOf<Report>()
        session.writeTransaction().use { tx ->
            val partnerBirthDate = context.today().minusYears(context.model.ageOfAdulthood.toLong())
            val women = matchPartner(tx, region, partnerBirthDate, FEMALE).sorted(comparing { it.email }).collect(toList())
            val men = matchPartner(tx, region, partnerBirthDate, MALE).sorted(comparing { it.email }).collect(toList())
            random.randomPairs(women, men).forEach { pair: Pair<Person, Person> ->
                val licence = pair.first().email + pair.second().email
                val inserted = insertMarriage(tx, pair.first().email, pair.second().email, licence, context.today())
                if (context.isReporting) {
                    requireNotNull(inserted)
                    reports.add(Report(
                        input = listOf(pair.first().email, pair.second().email, licence, context.today()),
                        output = listOf(inserted)
                    ))
                } else assert(inserted == null)
            }
            tx.commit()
        }
        return reports
    }

    protected abstract fun matchPartner(tx: TX, country: Country, birthDate: LocalDateTime, gender: Gender): Stream<Person>

    protected abstract fun insertMarriage(
        tx: TX, wifeEmail: String, husbandEmail: String, marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage?
}
