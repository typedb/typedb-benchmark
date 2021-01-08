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

public abstract class InsertRelocationAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final World.City city;
    protected final LocalDateTime today;
    protected final String relocateeEmail;
    protected final String relocationCityName;

    public InsertRelocationAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today, String relocateeEmail, String relocationCityName) {
        super(dbOperation);
        this.city = city;
        this.today = today;
        this.relocateeEmail = relocateeEmail;
        this.relocationCityName = relocationCityName;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(city, today, relocateeEmail, relocationCityName);
    }

    public enum InsertRelocationActionField implements ComparableField {
        PERSON_EMAIL, OLD_CITY_NAME, NEW_CITY_NAME, RELOCATION_DATE
    }
}
