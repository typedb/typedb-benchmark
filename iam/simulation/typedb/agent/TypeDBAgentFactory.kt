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

import com.vaticle.typedb.iam.simulation.agent.AgentFactory
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBDriver

class TypeDBAgentFactory(client: TypeDBDriver, context: Context) : AgentFactory<TypeDBDriver>(client, context) {

    override fun createPersonAgent(client: TypeDBDriver, context: Context) = TypeDBPersonAgent(client, context)
    override fun createFriendshipAgent(client: TypeDBDriver, context: Context) = TypeDBFriendshipAgent(client, context)
    override fun createMarriageAgent(client: TypeDBDriver, context: Context) = TypeDBMarriageAgent(client, context)
    override fun createParenthoodAgent(client: TypeDBDriver, context: Context) = TypeDBParenthoodAgent(client, context)
    override fun createLineageAgent(client: TypeDBDriver, context: Context) = TypeDBLineageAgent(client, context)
    override fun createNationalityAgent(client: TypeDBDriver, context: Context) = TypeDBNationalityAgent(client, context)
    override fun createCitizenshipAgent(client: TypeDBDriver, context: Context) = TypeDBCitizenshipAgent(client, context)
    override fun createMaritalStatusAgent(client: TypeDBDriver, context: Context) = TypeDBMaritalStatusAgent(client, context)
    override fun createCoupleFriendshipAgent(client: TypeDBDriver, context: Context) = TypeDBCoupleFriendshipAgent(client, context)
    override fun createGrandparenthoodAgent(client: TypeDBDriver, context: Context) = TypeDBGrandparenthoodAgent(client, context)
}
