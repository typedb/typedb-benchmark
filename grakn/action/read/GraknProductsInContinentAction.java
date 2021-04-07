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

package grakn.benchmark.grakn.action.read;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.action.read.ProductsInContinentAction;
import grakn.benchmark.simulation.world.World;
import graql.lang.query.GraqlMatch;

import java.util.List;

import static grakn.benchmark.grakn.action.Model.CONTINENT;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PRODUCED_IN;
import static grakn.benchmark.grakn.action.Model.PRODUCED_IN_CONTINENT;
import static grakn.benchmark.grakn.action.Model.PRODUCED_IN_PRODUCT;
import static grakn.benchmark.grakn.action.Model.PRODUCT;
import static grakn.benchmark.grakn.action.Model.PRODUCT_BARCODE;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknProductsInContinentAction extends ProductsInContinentAction<GraknTransaction> {

    public GraknProductsInContinentAction(GraknTransaction tx, World.Continent continent) {
        super(tx, continent);
    }

    @Override
    public List<Long> run() {
        GraqlMatch.Unfiltered query = query(continent.name());
        return tx.sortedExecute(query, PRODUCT_BARCODE, null);
    }

    public static GraqlMatch.Unfiltered query(String continentName) {
        return match(
                var(CONTINENT)
                        .isa(CONTINENT)
                        .has(LOCATION_NAME, continentName),
                var(PRODUCT)
                        .isa(PRODUCT)
                        .has(PRODUCT_BARCODE, var(PRODUCT_BARCODE)),
                var(PRODUCED_IN)
                        .rel(PRODUCED_IN_PRODUCT, var(PRODUCT))
                        .rel(PRODUCED_IN_CONTINENT, var(CONTINENT))
                        .isa(PRODUCED_IN)

        );
    }
}
