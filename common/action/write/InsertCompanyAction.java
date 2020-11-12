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

package grakn.simulation.common.action.write;

import grakn.simulation.common.action.Action;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertCompanyAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {

    protected final World.Country country;
    protected final LocalDateTime today;
    protected final int companyNumber;
    protected final String companyName;

    public InsertCompanyAction(DB_OPERATION dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        super(dbOperation);
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
