package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.world.ContinentAgent;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class ProductAgent extends ContinentAgent {

    @Override
    public final AgentResult iterate() {
        int numProducts = world().getScaleFactor();
        for (int i = 0; i < numProducts; i++) {
            String productName = randomAttributeGenerator().boundRandomLengthRandomString(5, 20);
            String productDescription = randomAttributeGenerator().boundRandomLengthRandomString(75, 100);
            Double barcode = (double) uniqueId(i);
            try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertProduct"))) {
                insertProduct(barcode, productName, productDescription);
            }
        }
        commitTxWithTracing();
        return null;
    }

    abstract protected void insertProduct(Double barcode, String productName, String productDescription);

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered getProductsInContinentQuery(World.Continent continent);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(0, world().getScaleFactor());
    }
}