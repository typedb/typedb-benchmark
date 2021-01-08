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

package grakn.benchmark.common.action.read;

import grakn.benchmark.common.action.Action;
import grakn.benchmark.common.action.SpouseType;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MarriedCoupleAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, List<HashMap<SpouseType, String>>> {
    protected final World.City city;
    protected final LocalDateTime today;

    public MarriedCoupleAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation);
        this.city = city;
        this.today = today;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return Action.argsList(city, today);
    }
}
