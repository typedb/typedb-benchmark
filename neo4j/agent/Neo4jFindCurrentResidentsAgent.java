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
import grakn.benchmark.simulation.agent.FindCurrentResidentsAgent;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;

public class Neo4jFindCurrentResidentsAgent extends FindCurrentResidentsAgent<Neo4jTransaction> {

    public Neo4jFindCurrentResidentsAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void matchCurrentResidents(Neo4jTransaction tx) {
//        Finds only those who currently live in Berlin
//        This means those who were born in Berlin and never relocated Berlin, or whose last relocation was to Berlin
        String query = "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: \"Berlin\"})\n" +
                "WHERE NOT (person)-[:RELOCATED_TO]->()\n" +
                "RETURN person.email AS email\n" +
                "UNION\n" +
                "MATCH (person:Person)-[relocatedTo:RELOCATED_TO]->(city:City)\n" +
                "WITH person, city, relocatedTo.relocationDate AS relocDate\n" +
                "ORDER BY relocDate DESC\n" +
                "WITH person.email AS email, collect(relocDate)[0] AS lastRelocDate, collect(city)[0] as lastCity\n" +
                "WHERE lastCity.locationName = \"Berlin\"\n" +
                "RETURN email;";
        tx.sortedExecute(new Query(query), "email", null);
    }
}
