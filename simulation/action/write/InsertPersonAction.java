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

package grakn.benchmark.simulation.action.write;

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertPersonAction<TX extends Transaction, ACTION_RETURN_TYPE> extends Action<TX, ACTION_RETURN_TYPE> {
    protected final World.City worldCity;
    protected final LocalDateTime today;
    protected final String email;
    protected final String gender;
    protected final String forename;
    protected final String surname;

    public InsertPersonAction(TX tx, World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        super(tx);
        this.worldCity = city;
        this.today = today;
        this.email = email;
        this.gender = gender;
        this.forename = forename;
        this.surname = surname;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(worldCity, today, email, gender, forename, surname);
    }

    public enum InsertPersonActionField implements ComparableField {
        EMAIL, GENDER, FORENAME, SURNAME, DATE_OF_BIRTH
    }
}
