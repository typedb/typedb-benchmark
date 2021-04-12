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

package grakn.benchmark.common;

import javax.annotation.Nullable;

public enum DatabaseType {
    GRAKN_CORE("grakn-core", "Grakn Core", "localhost:1729"),
    GRAKN_CLUSTER("grakn-cluster", "Grakn Cluster", "localhost:1729"),
    NEO4J("neo4j", "Neo4j", "bolt://localhost:7687");

    private final String key;
    private final String fullname;
    private final String defaultAddress;

    DatabaseType(String key, String fullname, String defaultAddress) {
        this.key = key;
        this.fullname = fullname;
        this.defaultAddress = defaultAddress;
    }

    @Nullable
    public static DatabaseType of(String key) {
        for (DatabaseType value : values()) {
            if (value.key.equals(key)) return value;
        }
        return null;
    }

    public String key() {
        return key;
    }

    public String fullname() {
        return fullname;
    }

    public String defaultAddress() {
        return defaultAddress;
    }

    public boolean isGraknCore() {
        return key.equals("grakn-core");
    }

    public boolean isGraknCluster() {
        return key.equals("grakn-cluster");
    }

    public boolean isNeo4j() {
        return key.equals("neo4j");
    }
}
