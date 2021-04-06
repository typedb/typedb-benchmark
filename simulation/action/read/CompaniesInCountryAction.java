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

package grakn.benchmark.simulation.action.read;

import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class CompaniesInCountryAction<TX extends Transaction> extends ReadAction<TX, List<Long>> {

    protected final World.Country country;
    protected final int numCompanies;

    public CompaniesInCountryAction(TX dbOperation, World.Country country, int numCompanies) {
        super(dbOperation);
        this.country = country;
        this.numCompanies = numCompanies;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(country, numCompanies);
    }
}
