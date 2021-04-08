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
import grakn.benchmark.simulation.action.SpouseType;
import grakn.benchmark.simulation.driver.Transaction;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class InsertParentShipAction<TX extends Transaction, ACTION_RETURN_TYPE> extends Action<TX, ACTION_RETURN_TYPE> {
    protected final HashMap<SpouseType, String> marriage;
    protected final String childEmail;

    public InsertParentShipAction(TX tx, HashMap<SpouseType, String> marriage, String childEmail) {
        super(tx);
        this.marriage = marriage;
        this.childEmail = childEmail;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(marriage, childEmail);
    }

    public enum InsertParentShipActionField implements ComparableField {
        HUSBAND_EMAIL, WIFE_EMAIL, CHILD_EMAIL
    }
}
