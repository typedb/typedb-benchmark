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

package grakn.simulation.neo4j.action.write;

import grakn.simulation.common.action.write.InsertProductAction;
import grakn.simulation.common.world.World;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.HashMap;

import static grakn.simulation.neo4j.action.Model.LOCATION_NAME;
import static grakn.simulation.neo4j.action.Model.PRODUCT_BARCODE;
import static grakn.simulation.neo4j.action.Model.PRODUCT_DESCRIPTION;
import static grakn.simulation.neo4j.action.Model.PRODUCT_NAME;

public class Neo4jInsertProductAction extends InsertProductAction<Neo4jOperation, Record> {
    public Neo4jInsertProductAction(Neo4jOperation dbOperation, World.Continent continent, Long barcode, String productName, String productDescription) {
        super(dbOperation, continent, barcode, productName, productDescription);
    }

    @Override
    public Record run() {
        String template = query();
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("continentName", continent.name());
            put("barcode", barcode);
            put("productName", productName);
            put("description", productDescription);
        }};
        return singleResult(dbOperation.execute(new Query(template, parameters)));
    }

    public static String query() {
        return "MATCH (continent:Continent {locationName: $continentName})\n" +
                "CREATE (product:Product {\n" +
                "   barcode: $barcode,\n" +
                "   name: $productName,\n" +
                "   description: $description\n" +
                "})-[:PRODUCED_IN]->(continent)\n" +
                "RETURN product.barcode, product.name, product.description, continent.locationName";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertProductActionField.PRODUCT_BARCODE, answer.asMap().get("product." + PRODUCT_BARCODE));
            put(InsertProductActionField.PRODUCT_NAME, answer.asMap().get("product." + PRODUCT_NAME));
            put(InsertProductActionField.PRODUCT_DESCRIPTION, answer.asMap().get("product." + PRODUCT_DESCRIPTION));
            put(InsertProductActionField.CONTINENT, answer.asMap().get("continent." + LOCATION_NAME));
        }};
    }
}
