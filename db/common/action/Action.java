package grakn.simulation.db.common.action;

import grakn.simulation.db.common.operation.DbOperationController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;

public abstract class Action<DB_OPERATION extends DbOperationController.DbOperation, ACTION_RETURN_TYPE> {

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
        return this.getClass().getSimpleName();
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
    }

    public interface ComparableField {}
}
