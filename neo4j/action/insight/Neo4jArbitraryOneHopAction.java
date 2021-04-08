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

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.action.insight.ArbitraryOneHopAction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.List;

public class Neo4jArbitraryOneHopAction extends ArbitraryOneHopAction<Neo4jTransaction> {

    public Neo4jArbitraryOneHopAction(Neo4jTransaction tx) {
        super(tx);
    }

    @Override
    public Void run() {
        List<Record> results = tx.execute(new Query(query()));
        return null;
    }

    public static String query() {
//        TODO Needs to do inference to be equivalent to Grakn
        return "MATCH (person:Person {email: \"" + PERSON_EMAIL_FOR_QUERY + "\"})--(x)\n" +
                "RETURN x";
    }
}
