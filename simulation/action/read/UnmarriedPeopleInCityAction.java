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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class UnmarriedPeopleInCityAction<TX extends Transaction> extends ReadAction<TX, List<String>> {
    protected final World.City city;
    protected final String gender;
    protected final LocalDateTime dobOfAdults;

    public UnmarriedPeopleInCityAction(TX tx, World.City city, String gender, LocalDateTime dobOfAdults) {
        super(tx);
        this.city = city;
        this.gender = gender;
        this.dobOfAdults = dobOfAdults;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(city, gender, dobOfAdults);
    }
}
