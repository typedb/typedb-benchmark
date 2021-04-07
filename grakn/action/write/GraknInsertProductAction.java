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

package grakn.benchmark.grakn.action.write;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.action.write.InsertProductAction;
import grakn.benchmark.simulation.world.World;
import grakn.client.api.answer.ConceptMap;
import graql.lang.query.GraqlInsert;

import java.util.HashMap;

import static grakn.benchmark.grakn.action.Model.CONTINENT;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PRODUCED_IN;
import static grakn.benchmark.grakn.action.Model.PRODUCED_IN_CONTINENT;
import static grakn.benchmark.grakn.action.Model.PRODUCED_IN_PRODUCT;
import static grakn.benchmark.grakn.action.Model.PRODUCT;
import static grakn.benchmark.grakn.action.Model.PRODUCT_BARCODE;
import static grakn.benchmark.grakn.action.Model.PRODUCT_DESCRIPTION;
import static grakn.benchmark.grakn.action.Model.PRODUCT_NAME;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknInsertProductAction extends InsertProductAction<GraknTransaction, ConceptMap> {

    public GraknInsertProductAction(GraknTransaction tx, World.Continent continent, Long barcode, String productName, String productDescription) {
        super(tx, continent, barcode, productName, productDescription);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert insertProductQuery = query(continent.name(), barcode, productName, productDescription);
        return singleResult(tx.execute(insertProductQuery));
    }

    public static GraqlInsert query(String continentName, Long barcode, String productName, String productDescription) {
        return match(
                var(CONTINENT)
                        .isa(CONTINENT)
                        .has(LOCATION_NAME, continentName)
        ).insert(
                var(PRODUCT)
                        .isa(PRODUCT)
                        .has(PRODUCT_BARCODE, barcode)
                        .has(PRODUCT_NAME, productName)
                        .has(PRODUCT_DESCRIPTION, productDescription),
                var(PRODUCED_IN)
                        .rel(PRODUCED_IN_PRODUCT, var(PRODUCT))
                        .rel(PRODUCED_IN_CONTINENT, var(CONTINENT))
                        .isa(PRODUCED_IN)
        );
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertProductActionField.PRODUCT_BARCODE, tx.getOnlyAttributeOfThing(answer, PRODUCT, PRODUCT_BARCODE));
            put(InsertProductActionField.PRODUCT_NAME, tx.getOnlyAttributeOfThing(answer, PRODUCT, PRODUCT_NAME));
            put(InsertProductActionField.PRODUCT_DESCRIPTION, tx.getOnlyAttributeOfThing(answer, PRODUCT, PRODUCT_DESCRIPTION));
            put(InsertProductActionField.CONTINENT, tx.getOnlyAttributeOfThing(answer, CONTINENT, LOCATION_NAME));
        }};
    }
}
