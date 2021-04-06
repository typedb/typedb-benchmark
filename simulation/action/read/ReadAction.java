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

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.driver.Transaction;

import java.util.HashMap;

public abstract class ReadAction<DB_OPERATION extends Transaction, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    public ReadAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ACTION_RETURN_TYPE answer) {
        return new HashMap<ComparableField, Object>() {{
            put(ReadActionField.SINGLE_FIELD_ANSWER, answer);
        }};
    }

    public enum ReadActionField implements ComparableField {
        SINGLE_FIELD_ANSWER
    }
}
