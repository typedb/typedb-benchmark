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

package grakn.benchmark.neo4j.driver;

import grakn.benchmark.simulation.driver.Session;


public class Neo4jSession implements Session<Neo4jTransaction> {

    private final org.neo4j.driver.Session nativeSession;

    public Neo4jSession(org.neo4j.driver.Session nativeSession) {
        this.nativeSession = nativeSession;
    }

    @Override
    public Neo4jTransaction transaction(String tracker, long iteration, boolean trace) {
        return new Neo4jTransaction(nativeSession, tracker, iteration);
    }

    @Override
    public void close() {
        nativeSession.close();
    }
}
