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
import grakn.benchmark.simulation.common.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertCompanyAction<TX extends Transaction, ACTION_RETURN_TYPE> extends Action<TX, ACTION_RETURN_TYPE> {

    protected final World.Country country;
    protected final LocalDateTime today;
    protected final int companyNumber;
    protected final String companyName;

    public InsertCompanyAction(TX tx, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        super(tx);
        this.country = country;
        this.today = today;
        this.companyNumber = companyNumber;
        this.companyName = companyName;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(country, today, companyNumber, companyName);
    }

    public enum InsertCompanyActionField implements ComparableField {
        COMPANY_NUMBER, COMPANY_NAME, DATE_OF_INCORPORATION, COUNTRY
    }
}
