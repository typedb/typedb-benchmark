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

package grakn.benchmark.neo4j.action.insight;

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.action.insight.FindSpecificMarriageAction;
import org.neo4j.driver.Query;

import java.util.stream.Collectors;

public class Neo4jFindSpecificMarriageAction extends FindSpecificMarriageAction<Neo4jTransaction> {

    public Neo4jFindSpecificMarriageAction(Neo4jTransaction tx) {
        super(tx);
    }

    @Override
    public String run() {
        return optionalSingleResult(tx.execute(new Query(query())).stream().map(ans -> ans.get("marriedTo.marriageId").asString()).collect(Collectors.toList()));
    }

    public static String query() {
        return "MATCH ()-[marriedTo:MARRIED_TO {marriageId: \"" + MARRIAGE_ID_FOR_QUERY + "\"}]-()\n" +
                "RETURN marriedTo.marriageId";
    }
}
