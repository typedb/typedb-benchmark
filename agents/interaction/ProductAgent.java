package grakn.simulation.agents.interaction;

import grakn.simulation.agents.world.ContinentAgent;

public abstract class ProductAgent extends ContinentAgent {

    @Override
    public final void iterate() {
        int numProducts = world().getScaleFactor();
        for (int i = 0; i < numProducts; i++) {
            insertProduct(i);
        }
        tx().commit();
    }

    abstract protected void insertProduct(int iterationScopeId);

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered getProductsInContinentQuery(World.Continent continent);
}