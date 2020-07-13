package grakn.simulation.agents.interaction;

import grakn.simulation.agents.world.ContinentAgent;

public abstract class ProductAgent extends ContinentAgent {

    @Override
    public final void iterate() {
        int numProducts = world().getScaleFactor();
        for (int i = 0; i < numProducts; i++) {
            String productName = randomAttributeGenerator().boundRandomLengthRandomString(5, 20);
            String productDescription = randomAttributeGenerator().boundRandomLengthRandomString(75, 100);
            insertProduct(i, productName, productDescription);
        }
        tx().commit();
    }

    abstract protected void insertProduct(int iterationScopeId, String productName, String productDescription);

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered getProductsInContinentQuery(World.Continent continent);
}