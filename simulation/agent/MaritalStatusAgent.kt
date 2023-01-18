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

abstract class MaritalStatusAgent<TX: Transaction> protected constructor(client: Client<Session<TX>>, context: Context) :
    Agent<Country, TX>(client, context) {
    override val agentClass = MaritalStatusAgent::class.java
    override val regions = context.seedData.countries

    override fun run(session: Session<TX>, region: Country, random: RandomSource): List<Report> {
        if (context.isReporting) throw RuntimeException("Reports are not comparable for reasoning agents.")
        session.reasoningTransaction().use { tx ->
            // Pick people who are exactly old enough to be married since this determines their status
            matchMaritalStatus(tx, region, context.today().minusYears(context.ageOfAdulthood.toLong()))
        }
        return emptyList()
    }

    protected abstract fun matchMaritalStatus(tx: TX, country: Country, marriageBirthDate: LocalDateTime)
}
