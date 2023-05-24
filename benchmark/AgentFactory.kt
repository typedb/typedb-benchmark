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

import com.vaticle.typedb.benchmark.common.Context
import com.vaticle.typedb.simulation.Agent
import com.vaticle.typedb.simulation.typedb.TypeDBClient

class AgentFactory(client: TypeDBClient, context: Context): com.vaticle.typedb.simulation.Agent.Factory() {
    override val map: Map<Class<out Agent<*, *, *>>, () -> Agent<*, *, *>> = mapOf(
        PersonAgent::class.java to { person(client, context) },
    )

    fun person(client: TypeDBClient, context: Context) = PersonAgent(client, context)

}
