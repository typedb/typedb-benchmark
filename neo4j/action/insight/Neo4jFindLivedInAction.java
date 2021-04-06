/*
 * Copyright (C) 2020 Grakn Labs
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

package grakn.benchmark.neo4j.action.insight;

import grakn.benchmark.neo4j.driver.Neo4jOperation;
import grakn.benchmark.simulation.action.insight.FindLivedInAction;
import org.neo4j.driver.Query;

import java.util.List;

public class Neo4jFindLivedInAction extends FindLivedInAction<Neo4jOperation> {
    public Neo4jFindLivedInAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(new Query(query()), "person.email", null);
    }

    public static String query() {
        return "MATCH (person:Person)-[livedIn]->(city:City {locationName: \"Berlin\"})\n" +
                "WHERE TYPE(livedIn) = \"BORN_IN\" OR TYPE(livedIn) = \"RELOCATED_TO\"\n" +
                "RETURN person.email";
    }
}
