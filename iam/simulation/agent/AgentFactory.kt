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
package com.vaticle.typedb.iam.simulation.agent

import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.simulation.Agent
import com.vaticle.typedb.simulation.common.DBClient

abstract class AgentFactory<CLIENT: DBClient<*>>(client: CLIENT, context: Context) : Agent.Factory() {

    override val map: Map<Class<out Agent<*, *, *>>, () -> Agent<*, *, *>> = mapOf(
        PersonAgent::class.java to { createPersonAgent(client, context) },
        FriendshipAgent::class.java to { createFriendshipAgent(client, context) },
        MarriageAgent::class.java to { createMarriageAgent(client, context) },
        ParenthoodAgent::class.java to { createParenthoodAgent(client, context) },
        MaritalStatusAgent::class.java to { createMaritalStatusAgent(client, context) },
        GrandparenthoodAgent::class.java to { createGrandparenthoodAgent(client, context) },
        LineageAgent::class.java to { createLineageAgent(client, context) },
        NationalityAgent::class.java to { createNationalityAgent(client, context) },
        CitizenshipAgent::class.java to { createCitizenshipAgent(client, context) },
        CoupleFriendshipAgent::class.java to { createCoupleFriendshipAgent(client, context) },
    )

    protected abstract fun createPersonAgent(client: CLIENT, context: Context): PersonAgent<*>
    protected abstract fun createFriendshipAgent(client: CLIENT, context: Context): FriendshipAgent<*>
    protected abstract fun createMarriageAgent(client: CLIENT, context: Context): MarriageAgent<*>
    protected abstract fun createParenthoodAgent(client: CLIENT, context: Context): ParenthoodAgent<*>
    protected abstract fun createLineageAgent(client: CLIENT, context: Context): LineageAgent<*>
    protected abstract fun createNationalityAgent(client: CLIENT, context: Context): NationalityAgent<*>
    protected abstract fun createCitizenshipAgent(client: CLIENT, context: Context): CitizenshipAgent<*>
    protected abstract fun createMaritalStatusAgent(client: CLIENT, context: Context): MaritalStatusAgent<*>
    protected abstract fun createCoupleFriendshipAgent(client: CLIENT, context: Context): CoupleFriendshipAgent<*>
    protected abstract fun createGrandparenthoodAgent(client: CLIENT, context: Context): GrandparenthoodAgent<*>
}
