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
package com.vaticle.typedb.benchmark.common.params

enum class DatabaseType(val key: String, val fullname: String, val defaultAddress: String) {
    TYPEDB("typedb", "TypeDB", "localhost:1729"),
    TYPEDB_CLUSTER("typedb-cluster", "TypeDB Cluster", "localhost:1729"),
    NEO4J("neo4j", "Neo4j", "bolt://localhost:7687");

    companion object {
        private val validValues = values().joinToString(", ") { it.key }

        fun of(key: String): DatabaseType {
            return values().find { it.key == key }
                ?: throw IllegalArgumentException("Unexpected database type: '$key'. Allowed database types are: $validValues")
        }
    }
}
