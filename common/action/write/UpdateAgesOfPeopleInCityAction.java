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

import grakn.simulation.common.action.read.ReadAction;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class UpdateAgesOfPeopleInCityAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, Integer> {
    protected final LocalDateTime today;
    protected final World.City city;

    public UpdateAgesOfPeopleInCityAction(DB_OPERATION dbOperation, LocalDateTime today, World.City city) {
        super(dbOperation);
        this.today = today;
        this.city = city;
    }

    @Override
    public HashMap<ComparableField, Object> outputForReport(Integer answer) {
        return new HashMap<>(); // Nothing to report for this action
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(today, city);
    }
}
