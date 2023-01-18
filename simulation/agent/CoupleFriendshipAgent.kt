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
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.common.seed.RandomSource
import com.vaticle.typedb.benchmark.simulation.driver.Client
import com.vaticle.typedb.benchmark.simulation.driver.Session
import com.vaticle.typedb.benchmark.simulation.driver.Transaction
import java.time.LocalDateTime

abstract class CoupleFriendshipAgent<TX: Transaction> protected constructor(
    client: Client<Session<TX>>, context: Context
) : Agent<Country, TX>(client, context) {
    override val agentClass = CoupleFriendshipAgent::class.java
    override val regions = context.seedData.countries

    override fun run(session: Session<TX>, region: Country, random: RandomSource): List<Report> {
        // This agent targets the expense of the `put` operation of reasoning. More specifically the cost of `get` to
        // check whether a relation is pre-existing
        if (context.isReporting) throw RuntimeException("Reports are not comparable for reasoning agents.")
        session.reasoningTransaction().use { tx -> matchFriendships(tx, region, context.today()) }
        return emptyList()
    }

    protected abstract fun matchFriendships(tx: TX, country: Country, marriageBirthDate: LocalDateTime)
}
