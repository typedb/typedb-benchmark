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
package com.vaticle.typedb.iam.simulation.neo4j.agent

import com.vaticle.typedb.iam.simulation.agent.AgentFactory
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.simulation.neo4j.Neo4jClient

class Neo4jAgentFactory(client: Neo4jClient, context: Context) : AgentFactory<Neo4jClient>(client, context) {

    override fun createPersonAgent(client: Neo4jClient, context: Context) = Neo4jPersonAgent(client, context)
    override fun createFriendshipAgent(client: Neo4jClient, context: Context) = Neo4jFriendshipAgent(client, context)
    override fun createMarriageAgent(client: Neo4jClient, context: Context) = Neo4jMarriageAgent(client, context)
    override fun createParenthoodAgent(client: Neo4jClient, context: Context) = Neo4jParenthoodAgent(client, context)

    override fun createLineageAgent(client: Neo4jClient, context: Context): Nothing {
        throw unsupportedReasoningAgentException("LineageAgent")
    }

    override fun createNationalityAgent(client: Neo4jClient, context: Context): Nothing {
        throw unsupportedReasoningAgentException("NationalityAgent")
    }

    override fun createCitizenshipAgent(client: Neo4jClient, context: Context): Nothing {
        throw unsupportedReasoningAgentException("CitizenshipAgent")
    }

    override fun createMaritalStatusAgent(client: Neo4jClient, context: Context): Nothing {
        throw unsupportedReasoningAgentException("MaritalStatusAgent")
    }

    override fun createCoupleFriendshipAgent(client: Neo4jClient, context: Context): Nothing {
        throw unsupportedReasoningAgentException("CoupleFriendshipAgent")
    }

    override fun createGrandparenthoodAgent(client: Neo4jClient, context: Context): Nothing {
        throw unsupportedReasoningAgentException("GrandparenthoodAgent")
    }

    companion object {
        fun unsupportedReasoningAgentException(agentName: String): UnsupportedOperationException {
            return UnsupportedOperationException("$agentName requires reasoning, which is not supported by Neo4j")
        }
    }
}
