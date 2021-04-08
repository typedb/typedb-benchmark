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

package grakn.benchmark.simulation.action.read;

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.driver.Transaction;

import java.util.HashMap;

public abstract class ReadAction<TX extends Transaction, ANSWER> extends Action<TX, ANSWER> {

    public ReadAction(TX tx) {
        super(tx);
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ANSWER answer) {
        return new HashMap<>() {{
            put(ReadActionField.SINGLE_FIELD_ANSWER, answer);
        }};
    }

    public enum ReadActionField implements ComparableField {
        SINGLE_FIELD_ANSWER
    }
}
