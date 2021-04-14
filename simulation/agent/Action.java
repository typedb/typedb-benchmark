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

package grakn.benchmark.simulation.agent;

import grakn.benchmark.simulation.driver.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Iterables.getOnlyElement;

public abstract class Action<TX extends Transaction, ANSWER> {

    protected TX tx;

    public Action(TX tx) {
        this.tx = tx;
    }

    public static <A> A singleResult(List<A> answers) {
        return getOnlyElement(answers);
    }

    public static <A> A optionalSingleResult(List<A> answers) {
        if (answers.size() == 0) return null;
        else return getOnlyElement(answers);
    }

    public String name() {
        // We want the name of the abstract action that each backend implements
        return this.getClass().getSuperclass().getSimpleName();
    }

    public abstract ANSWER run();

    protected abstract HashMap<ComparableField, Object> outputForReport(ANSWER answer);

    protected abstract ArrayList<Object> inputForReport();

    protected static ArrayList<Object> argsList(Object... args) {
        return new ArrayList<>(Arrays.asList(args));
    }

    public Report report(ANSWER answer) {
        return new Report(answer);
    }

    public class Report {

        ArrayList<Object> input;
        HashMap<ComparableField, Object> output;

        public Report(ANSWER answer) {
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
            return "Report " + name() + " {input=" + input + ", output=" + output + "}";
        }
    }

    public interface ComparableField {}
}
