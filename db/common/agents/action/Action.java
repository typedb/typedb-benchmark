package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.DbOperationController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;

public abstract class Action<DB_OPERATION extends DbOperationController.DbOperation, ACTION_RETURN_TYPE> {

    protected DB_OPERATION dbOperation;
    protected ArrayList<Object> inputArgs;

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

    public abstract HashMap<String, Object> outputForReport(ACTION_RETURN_TYPE answer);

    public Report report(ACTION_RETURN_TYPE answer) {
        return new Report(answer);
    }

    public class Report {
        ArrayList<Object> input;
        HashMap<String, Object> output;

        public Report(ACTION_RETURN_TYPE answer) {
            this.input = inputArgs;
            this.output = outputForReport(answer);
        }
    }
}
