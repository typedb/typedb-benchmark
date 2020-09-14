package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.region.ContinentAgent;
import grakn.simulation.db.common.context.DatabaseContext;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class ProductAgent<CONTEXT extends DatabaseContext> extends ContinentAgent<CONTEXT> {

    @Override
    public final AgentResultSet iterate() {
        int numProducts = world().getScaleFactor();
        startAction();
        for (int i = 0; i < numProducts; i++) {
            String productName = randomAttributeGenerator().boundRandomLengthRandomString(5, 20);
            String productDescription = randomAttributeGenerator().boundRandomLengthRandomString(75, 100);
            Double barcode = (double) uniqueId(i);
            try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertProduct"))) {
                insertProduct(barcode, productName, productDescription);
            }
        }
        commitAction();
        return null;
    }

    abstract protected void insertProduct(Double barcode, String productName, String productDescription);

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered getProductsInContinentQuery(World.Continent continent);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(0, world().getScaleFactor());
    }
}