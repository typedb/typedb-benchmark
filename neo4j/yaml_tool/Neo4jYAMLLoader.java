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

package grakn.benchmark.neo4j.yaml_tool;

import grakn.benchmark.common.yaml_tool.QueryTemplate;
import grakn.benchmark.common.yaml_tool.YAMLLoader;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Neo4jYAMLLoader extends YAMLLoader {

    private final Session session;

    public Neo4jYAMLLoader(Session session, Map<String, Path> accessibleFiles) {
        super(accessibleFiles);
        this.session = session;
    }

    @Override
    protected void parseCSV(QueryTemplate template, CSVParser parser) throws IOException {
        Transaction tx = session.beginTransaction();
        for (CSVRecord record : parser.getRecords()) {
            Query interpolatedQuery = new Query(template.interpolate(record::get));
            tx.run(interpolatedQuery);
        }
        tx.commit();
    }
}
