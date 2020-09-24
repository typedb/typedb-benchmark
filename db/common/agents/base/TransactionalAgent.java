package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.agents.utils.CheckMethod;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.context.DatabaseTransaction;
import grakn.simulation.db.common.world.Region;

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

    protected AgentResult results(DB_ANSWER_TYPE answer) {
        if (test()) { // testing is active
            return resultsForTesting(answer);
        } else {
            return null;
        }
    }
}
