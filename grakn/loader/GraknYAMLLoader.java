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

package grakn.benchmark.grakn.loader;

import grakn.benchmark.simulation.loader.QueryTemplate;
import grakn.benchmark.simulation.loader.YAMLLoader;
import grakn.client.api.GraknSession;
import grakn.client.api.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class GraknYAMLLoader extends YAMLLoader {

    private final GraknSession session;

    public GraknYAMLLoader(GraknSession session, Map<String, Path> accessibleFiles) {
        super(accessibleFiles);
        this.session = session;
    }

    @Override
    protected void parseCSV(QueryTemplate template, CSVParser parser) throws IOException {
        try (GraknTransaction tx = session.transaction(GraknTransaction.Type.WRITE)) {
            for (CSVRecord record : parser.getRecords()) {
                String interpolatedQuery = template.interpolate(record::get);
                GraqlInsert insert = Graql.parseQuery(interpolatedQuery);
                tx.query().insert(insert);
            }
            tx.commit();
        }
    }
}
