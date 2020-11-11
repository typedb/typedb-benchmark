package grakn.simulation.common.action;

import grakn.simulation.common.driver.DbOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Iterables.getOnlyElement;

public abstract class Action<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> {

    protected DB_OPERATION dbOperation;

    public Action(DB_OPERATION dbOperation) {
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Report report = (Report) o;
            return input.equals(report.input) &&
                    output.equals(report.output);
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
