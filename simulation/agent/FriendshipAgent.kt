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
import kotlin.math.ln

abstract class FriendshipAgent<TX: Transaction> protected constructor(client: Client<Session<TX>>, context: Context) :
    Agent<Country, TX>(client, context) {
    override val agentClass = FriendshipAgent::class.java
    override val regions = context.seedData.countries

    override fun run(session: Session<TX>, region: Country, random: RandomSource): List<Report> {
        val reports = mutableListOf<Report>()
        session.writeTransaction().use { tx ->
            val birthDate = context.today().minusYears(context.ageOfFriendship.toLong())
            val teenagers = matchTeenagers(tx, region, birthDate).sorted(comparing { it.email }).collect(toList())
            random.randomPairs(teenagers, log2(context.scaleFactor).coerceAtMost(1)).forEach { friends ->
                val inserted = insertFriends(tx, friends.first().email, friends.second().email)
                if (context.isReporting) {
                    requireNotNull(inserted)
                    reports.add(Report(
                        input = listOf(friends.first().email, friends.second().email),
                        output = listOf(inserted.first(), inserted.second())
                    ))
                } else assert(inserted == null)
            }
            tx.commit()
        }
        return reports
    }

    protected abstract fun matchTeenagers(tx: TX, country: Country, birthDate: LocalDateTime): Stream<Person>
    protected abstract fun insertFriends(tx: TX, email1: String, email2: String): Pair<Person, Person>?

    companion object {
        fun log2(x: Int): Int {
            return (ln(x.toDouble()) / ln(2.0)).toInt()
        }
    }
}
