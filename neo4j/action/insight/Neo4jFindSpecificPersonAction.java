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

package grakn.simulation.neo4j.action.insight;

import grakn.simulation.common.action.insight.FindSpecificPersonAction;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

public class Neo4jFindSpecificPersonAction extends FindSpecificPersonAction<Neo4jOperation> {
    public Neo4jFindSpecificPersonAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public String run() {
        Record result = singleResult(dbOperation.execute(new Query(query())));
        return result.get("person.email").asString();
    }

    public static String query() {
        return "MATCH (person:Person {email: \"" + PERSON_EMAIL_FOR_QUERY + "\"})" +
                "RETURN person.email";
    }
}
