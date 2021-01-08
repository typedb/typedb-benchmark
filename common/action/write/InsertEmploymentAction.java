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

package grakn.benchmark.common.action.write;

import grakn.benchmark.common.action.Action;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertEmploymentAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {

    protected final World.City worldCity;
    protected final String employeeEmail;
    protected final long companyNumber;
    protected final LocalDateTime employmentDate;
    protected final double wageValue;
    protected final String contractContent;
    protected final double contractedHours;

    public InsertEmploymentAction(DB_OPERATION dbOperation, World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        super(dbOperation);
        this.worldCity = city;
        this.employeeEmail = employeeEmail;
        this.companyNumber = companyNumber;
        this.employmentDate = employmentDate;
        this.wageValue = wageValue;
        this.contractContent = contractContent;
        this.contractedHours = contractedHours;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(worldCity, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    public enum InsertEmploymentActionField implements ComparableField {
        CITY_NAME, PERSON_EMAIL, COMPANY_NUMBER, START_DATE, WAGE, CURRENCY, CONTRACT_CONTENT, CONTRACTED_HOURS
    }
}
