package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.world.World;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

import grakn.simulation.db.common.agents.region.ContinentAgent;
import grakn.simulation.db.common.context.DbDriver;
import java.util.Random;

public class ProductAgent<DB_DRIVER extends DbDriver> extends ContinentAgent<DB_DRIVER> {

    public ProductAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
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
        protected void run(DbOperationController dbOperationController, World.Continent continent, SimulationContext simulationContext) {
            int numProducts = simulationContext.world().getScaleFactor();
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("insertProduct", tracker())) {
                for (int i = 0; i < numProducts; i++) {
                    String productName = randomAttributeGenerator().boundRandomLengthRandomString(5, 20);
                    String productDescription = randomAttributeGenerator().boundRandomLengthRandomString(75, 100);
                    Double barcode = (double) uniqueId(simulationContext, i);
                    runAction(dbOperationController.actionFactory().insertProduct(continent, barcode, productName, productDescription));
                }
                dbOperation.save();
            }
        }
    }
}
