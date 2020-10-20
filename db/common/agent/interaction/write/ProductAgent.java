package grakn.simulation.db.common.agent.interaction.write;

import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.agent.region.ContinentAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.util.Random;

public class ProductAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends ContinentAgent<DB_DRIVER, DB_OPERATION> {

    public ProductAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected RegionalProductAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalProductAgent(simulationStep, tracker, random, test);
    }

    public class RegionalProductAgent extends RegionalAgent {
        public RegionalProductAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.Continent continent, SimulationContext simulationContext) {
            int numProducts = simulationContext.world().getScaleFactor();
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                for (int i = 0; i < numProducts; i++) {
                    String productName = randomAttributeGenerator().boundRandomLengthRandomString(5, 20);
                    String productDescription = randomAttributeGenerator().boundRandomLengthRandomString(75, 100);
                    Double barcode = (double) uniqueId(simulationContext, i);
                    runAction(actionFactory().insertProductAction(dbOperation, continent, barcode, productName, productDescription));
                }
                dbOperation.save();
            }
        }
    }
}
