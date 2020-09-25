package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.agents.utils.CheckMethod;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.context.DatabaseTransaction;
import grakn.simulation.db.common.world.Region;

import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;

public abstract class TransactionalAgent<REGION extends Region, CONTEXT extends DatabaseContext<TRANSACTION>, TRANSACTION extends DatabaseTransaction, DB_ANSWER_TYPE> extends Agent<REGION, CONTEXT> {
    private TRANSACTION tx;
    private String action;

    public TRANSACTION tx() {
        return tx;
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public void newAction(String action) {
        CheckMethod.checkMethodExists(this, action);
        this.action = action;
        if (tx == null) {
            tx = backendContext().tx(getSessionKey(), logger(), tracker() + ":" + this.action);
        }
    }

    @Override
    public void closeAction() {
        tx().close();
        tx = null;
    }

    @Override
    public void commitAction() {
        tx().commit();
        tx = null;
    }

    public abstract AgentResult resultsForTesting(DB_ANSWER_TYPE answer);

    protected AgentResult single_result(List<DB_ANSWER_TYPE> answers) {
        if (test()) { // testing is active
            return resultsForTesting(getOnlyElement(answers));
        } else {
            return null;
        }
    }

    protected AgentResult optional_single_result(List<DB_ANSWER_TYPE> answers) {
        if (test()) { // testing is active
            if (answers.size() == 0) {
                return new AgentResult();
            } else {
                return resultsForTesting(getOnlyElement(answers));
            }
        } else {
            return null;
        }
    }
}
