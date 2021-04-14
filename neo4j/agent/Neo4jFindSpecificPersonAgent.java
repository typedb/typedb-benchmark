/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.neo4j.agent;

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.agent.FindSpecificPersonAgent;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;

public class Neo4jFindSpecificPersonAgent extends FindSpecificPersonAgent<Neo4jTransaction> {

    public Neo4jFindSpecificPersonAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void matchSpecificPerson(Neo4jTransaction tx, String personEmail) {
        String query = "MATCH (person:Person {email: \"" + personEmail + "\"}) RETURN person.email";
        tx.execute(new Query(query));
//        result.get("person.email").asString();
    }
}
