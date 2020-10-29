package grakn.simulation.db.common.agent.insight;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.ReadAction;
import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.agent.region.WorldAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.util.Random;

/**
 * Agent to perform a single action, where that action requires no parameters
 * @param <DB_DRIVER>
 * @param <DB_OPERATION>
 */
public abstract class WorldwideInsightAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends WorldAgent<DB_DRIVER, DB_OPERATION> {
    public WorldwideInsightAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected RegionalAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new WorldWideWorker(simulationStep, tracker, random, test);
    }

    protected abstract ReadAction<DB_OPERATION, ?> getAction(DB_OPERATION dbOperation);

    public class WorldWideWorker extends RegionalAgent {

        public WorldWideWorker(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World world, SimulationContext simulationContext) {
            for (int i = 0; i <= simulationContext.world().getScaleFactor(); i++) {
                try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                    runAction(getAction(dbOperation));
                }
            }
        }
    }
}
