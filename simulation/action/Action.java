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

package grakn.benchmark.simulation.action;

import grakn.benchmark.simulation.driver.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Iterables.getOnlyElement;

public abstract class Action<TX extends Transaction, ACTION_RETURN_TYPE> {

    protected TX dbOperation;

    public Action(TX dbOperation) {
        this.dbOperation = dbOperation;
    }

    public static <DB_ANSWER_TYPE> DB_ANSWER_TYPE singleResult(List<DB_ANSWER_TYPE> answers) {
        return getOnlyElement(answers);
    }

    public static <DB_ANSWER_TYPE> DB_ANSWER_TYPE optionalSingleResult(List<DB_ANSWER_TYPE> answers) {
        if (answers.size() == 0) {
            return null;
        } else {
            return getOnlyElement(answers);
        }
    }

    public String name() {
        // We want the name of the abstract action that each backend implements
        return this.getClass().getSuperclass().getSimpleName();
    }

    public abstract ACTION_RETURN_TYPE run();

    protected abstract HashMap<ComparableField, Object> outputForReport(ACTION_RETURN_TYPE answer);

    protected abstract ArrayList<Object> inputForReport();

    protected static ArrayList<Object> argsList(Object... args) {
        return new ArrayList<>(Arrays.asList(args));
    }

    public Report report(ACTION_RETURN_TYPE answer) {
        return new Report(answer);
    }

    public class Report {
        ArrayList<Object> input;
        HashMap<ComparableField, Object> output;

        public Report(ACTION_RETURN_TYPE answer) {
            this.input = inputForReport();
            this.output = outputForReport(answer);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Action<?, ?>.Report that = (Action<?, ?>.Report) object;
            return input.equals(that.input) && output.equals(that.output);
        }

        @Override
        public int hashCode() {
            return Objects.hash(input, output);
        }

        @Override
        public String toString() {
            return "Report " + name() +
                    " {" +
                    "input=" + input +
                    ", output=" + output +
                    "}";
        }
    }

    public interface ComparableField {}
}
