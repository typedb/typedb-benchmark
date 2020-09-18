package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.world.World;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface ProductAgentBase extends InteractionAgent<World.Continent> {

    @Override
    default AgentResultSet iterate(Agent<World.Continent, ?> agent, World.Continent continent, IterationContext iterationContext) {
        int numProducts = iterationContext.world().getScaleFactor();
        agent.startAction();
        for (int i = 0; i < numProducts; i++) {
            String productName = agent.randomAttributeGenerator().boundRandomLengthRandomString(5, 20);
            String productDescription = agent.randomAttributeGenerator().boundRandomLengthRandomString(75, 100);
            Double barcode = (double) agent.uniqueId(iterationContext, i);
            try (ThreadTrace trace = traceOnThread(agent.checkMethodTrace("insertProduct"))) {
                insertProduct(continent, barcode, productName, productDescription);
            }
        }
        agent.commitAction();
        return null;
    }

    void insertProduct(World.Continent continent, Double barcode, String productName, String productDescription);

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered getProductsInContinentQuery(World.Continent continent);

}