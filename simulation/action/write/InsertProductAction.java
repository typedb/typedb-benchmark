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

package grakn.benchmark.simulation.action.write;

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.common.GeoData;

import java.util.ArrayList;

public abstract class InsertProductAction<TX extends Transaction, ACTION_RETURN_TYPE> extends Action<TX, ACTION_RETURN_TYPE> {
    protected final GeoData.Continent continent;
    protected final Long barcode;
    protected final String productName;
    protected final String productDescription;

    public InsertProductAction(TX tx, GeoData.Continent continent, Long barcode, String productName, String productDescription) {
        super(tx);
        this.continent = continent;
        this.barcode = barcode;
        this.productName = productName;
        this.productDescription = productDescription;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(continent, barcode, productName, productDescription);
    }

    public enum InsertProductActionField implements ComparableField {
        PRODUCT_BARCODE, PRODUCT_NAME, PRODUCT_DESCRIPTION, CONTINENT
    }
}
