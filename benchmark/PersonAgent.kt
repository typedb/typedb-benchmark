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
package com.vaticle.typedb.benchmark

import com.vaticle.typedb.client.api.TypeDBSession
import com.vaticle.typedb.benchmark.common.Context
import com.vaticle.typedb.client.api.TypeDBTransaction
import com.vaticle.typedb.simulation.Agent
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typedb.simulation.typedb.TypeDBClient

import com.vaticle.typeql.lang.TypeQL
import java.time.LocalDateTime
import java.util.*

public class PersonAgent(client: TypeDBClient, context: Context) :
    Agent<Context.DBPartition, TypeDBSession, Context>(client, context) {

    override val agentClass = PersonAgent::class.java
    override val partitions = context.partitions

    val timeZero: LocalDateTime = LocalDateTime.now().withNano(0);

    private fun nameFrom(partitionId: Int, id: Int): String {
        return Objects.hash("name", partitionId, id).toString() + "-" + partitionId + ":" + id
    }

    private fun ssidFrom(partitionId: Int, id: Int): Long {
        return Objects.hash("ssid", partitionId, id).toLong()
    }

    private fun addressFrom(partitionId: Int, id: Int): String {
        return Objects.hash("address", partitionId, id).toString() + "-" + partitionId + ":" + id
    }

    private fun dateFrom(first: Int, second: Int): LocalDateTime {
        return timeZero.plusSeconds(Objects.hash("date", first, second).toLong())
    }

    fun createPerson(
        session: TypeDBSession,
        dbPartition: Context.DBPartition,
        randomSource: RandomSource
    ): List<Agent.Report> {
        session.transaction(TypeDBTransaction.Type.WRITE).use { tx ->
            for (i in 1..context.model.personPerBatch) {
                val id: Int = dbPartition.idCtr.addAndGet(1)
                tx.query().insert(TypeQL.insert(
                    TypeQL.`var`("p").isa("person")
                        .has("name", nameFrom(dbPartition.partitionId, id))
                        .has("ssid", ssidFrom(dbPartition.partitionId, id))
                        .has("address", addressFrom(dbPartition.partitionId, id))

                ))
            }
            tx.commit()
        }
        return listOf()
    }

    fun createFriendship(
        session: TypeDBSession,
        dbPartition: Context.DBPartition,
        randomSource: RandomSource
    ): List<Agent.Report> {
        session.transaction(TypeDBTransaction.Type.WRITE).use { tx ->
            for (i in 1..context.model.friendshipPerBatch) {
                val first : Int = 1 + randomSource.nextInt(dbPartition.idCtr.get())
                val second : Int = 1 + randomSource.nextInt(dbPartition.idCtr.get())
                tx.query().insert(TypeQL.match(
                    TypeQL.`var`("p1").isa("person").has("name", nameFrom(dbPartition.partitionId, first)),
                    TypeQL.`var`("p2").isa("person").has("name", nameFrom(dbPartition.partitionId, second)),
                    ).insert(
                        TypeQL.rel("person","p1").rel("person","p1").isa("friendship")
                            .has("meeting-time", dateFrom(first, second))
                    ))
            }
            tx.commit()
        }
        return listOf()
    }

    override val actionHandlers = mapOf(
        "createPerson" to ::createPerson,
        "createFriendship" to ::createFriendship
    )
}
