package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.world.ContinentAgent;

public abstract class ProductAgent extends ContinentAgent {

    @Override
    public final void iterate() {
        int numProducts = world().getScaleFactor();
        for (int i = 0; i < numProducts; i++) {
            String productName = randomAttributeGenerator().boundRandomLengthRandomString(5, 20);
            String productDescription = randomAttributeGenerator().boundRandomLengthRandomString(75, 100);
            Double barcode = (double) uniqueId(i);
            insertProduct(barcode, productName, productDescription);
        }
        tx().commitWithTracing();
    }

    abstract protected void insertProduct(Double barcode, String productName, String productDescription);

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered getProductsInContinentQuery(World.Continent continent);
}