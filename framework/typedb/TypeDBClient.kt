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
package com.vaticle.typedb.benchmark.framework.typedb

import com.vaticle.typedb.benchmark.framework.common.Partition
import com.vaticle.typedb.benchmark.framework.common.DBClient
import com.vaticle.typedb.client.TypeDB
import com.vaticle.typedb.client.api.TypeDBCredential
import com.vaticle.typedb.client.api.TypeDBSession
import com.vaticle.typedb.client.api.TypeDBSession.Type.DATA
import com.vaticle.typedb.client.api.TypeDBTransaction.Type.READ
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.`var`
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap

class TypeDBClient private constructor(
    private val nativeClient: com.vaticle.typedb.client.api.TypeDBClient,
    private val database: String
) : DBClient<TypeDBSession> {
    private val sessionMap = ConcurrentHashMap<String, TypeDBSession>()

    fun unpack(): com.vaticle.typedb.client.api.TypeDBClient {
        return nativeClient
    }

    override fun session(partition: Partition): TypeDBSession {
        return sessionMap.computeIfAbsent(partition.group) { nativeClient.session(database, DATA) }
    }

    override fun printStatistics(): String {
        val str = StringBuilder()
        nativeClient.session(database, DATA).use { session ->
            session.transaction(READ).use { tx ->
                val formatter = DecimalFormat("#,###")
                val numberOfEntities = tx.query().match(match(`var`("x").isa("entity")).count()).get().asLong()
                val numberOfAttributes = tx.query().match(match(`var`("x").isa("attribute")).count()).get().asLong()
                val numberOfRelations = tx.query().match(match(`var`("x").isa("relation")).count()).get().asLong()
                val numberOfThings = tx.query().match(match(`var`("x").isa("thing")).count()).get().asLong()
                str.append("Simulation statistic:").append("\n")
                str.append("\n")
                str.append("Count 'entity': ").append(formatter.format(numberOfEntities)).append("\n")
                str.append("Count 'relation': ").append(formatter.format(numberOfRelations)).append("\n")
                str.append("Count 'attribute': ").append(formatter.format(numberOfAttributes)).append("\n")
                if (numberOfThings != numberOfEntities + numberOfAttributes + numberOfRelations) {
                    str.append("The sum of 'entity', 'relation', and 'attribute' counts do not match the total 'thing' count: ")
                        .append(formatter.format(numberOfThings)).append("\n")
                } else {
                    str.append("Count 'thing' (total): ").append(formatter.format(numberOfThings)).append("\n")
                }
                str.append("\n")
            }
        }
        return str.toString()
    }

    override fun closeSessions() {
        sessionMap.values.forEach { it.close() }
        sessionMap.clear()
    }

    override fun close() {
        closeSessions()
        nativeClient.close()
    }

    companion object {
        fun core(hostUri: String, database: String): TypeDBClient {
            return TypeDBClient(TypeDB.coreClient(hostUri), database)
        }

        fun cluster(hostUri: String, database: String): TypeDBClient {
            return TypeDBClient(TypeDB.clusterClient(hostUri, TypeDBCredential("admin", "password", false)), database)
        }
    }
}
