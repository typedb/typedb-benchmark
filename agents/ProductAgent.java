package grakn.simulation.agents;

import grakn.client.exception.GraknClientException;
import grakn.simulation.agents.common.ContinentAgent;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

public class ProductAgent extends ContinentAgent {

    private static int NUM_PRODUCTS = 5;
    private static int NUM_INSERTION_ATTEMPTS = 5;

    @Override
    public void iterate() {
        for (int i = 0; i < NUM_PRODUCTS; i++) {
            insertProduct();
        }
        tx().commit();
    }

    private void insertProduct() {
        GraqlInsert insertProductQuery = Graql.match(
                Graql.var("continent")
                        .isa("continent")
                        .has("location-name", continent().name())
        ).insert(
                Graql.var("product")
                        .isa("product")
                        .has("product-barcode", randomAttributeGenerator().boundRandomDouble(0.0, 10000.0)) // TODO Handle key clashes
                        .has("product-name", randomAttributeGenerator().boundRandomLengthRandomString(5, 20))
                        .has("product-description", randomAttributeGenerator().boundRandomLengthRandomString(75, 100)),
                Graql.var("prod")
                        .isa("produced-in")
                        .rel("produced-in_product", Graql.var("product"))
                        .rel("produced-in_continent", Graql.var("continent"))
                );
        log().query("insertProduct", insertProductQuery);
        tx().execute(insertProductQuery);
    }
}