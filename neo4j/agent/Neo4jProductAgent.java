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
import grakn.benchmark.simulation.agent.ProductAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;

import java.util.HashMap;

public class Neo4jProductAgent extends ProductAgent<Neo4jTransaction> {

    public Neo4jProductAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void insertProduct(Neo4jTransaction tx, GeoData.Continent continent, long barcode, String productName, String productDescription) {
        String query = "MATCH (continent:Continent {locationName: $continentName})\n" +
                "CREATE (product:Product {\n" +
                "   barcode: $barcode,\n" +
                "   name: $productName,\n" +
                "   description: $description\n" +
                "})-[:PRODUCED_IN]->(continent)\n" +
                "RETURN product.barcode, product.name, product.description, continent.locationName";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("continentName", continent.name());
            put("barcode", barcode);
            put("productName", productName);
            put("description", productDescription);
        }};
        tx.execute(new Query(query, parameters));
    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
//        return new HashMap<ComparableField, Object>() {{
//            put(InsertProductActionField.PRODUCT_BARCODE, answer.asMap().get("product." + PRODUCT_BARCODE));
//            put(InsertProductActionField.PRODUCT_NAME, answer.asMap().get("product." + PRODUCT_NAME));
//            put(InsertProductActionField.PRODUCT_DESCRIPTION, answer.asMap().get("product." + PRODUCT_DESCRIPTION));
//            put(InsertProductActionField.CONTINENT, answer.asMap().get("continent." + LOCATION_NAME));
//        }};
//    }
}
