package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import static grakn.simulation.db.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN_CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN_PRODUCT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_BARCODE;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_DESCRIPTION;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_NAME;

public class ProductAgent extends grakn.simulation.db.common.agents.interaction.ProductAgent {

    @Override
    protected void insertProduct(Double barcode, String productName, String productDescription) {
        GraqlInsert insertProductQuery = Graql.match(
                Graql.var(CONTINENT)
                        .isa(CONTINENT)
                        .has(LOCATION_NAME, continent().name())
        ).insert(
                Graql.var(PRODUCT)
                        .isa(PRODUCT)
                        .has(PRODUCT_BARCODE, barcode)
                        .has(PRODUCT_NAME, productName)
                        .has(PRODUCT_DESCRIPTION, productDescription),
                Graql.var(PRODUCED_IN)
                        .isa(PRODUCED_IN)
                        .rel(PRODUCED_IN_PRODUCT, Graql.var(PRODUCT))
                        .rel(PRODUCED_IN_CONTINENT, Graql.var(CONTINENT))
                );
        log().query("insertProduct", insertProductQuery);
        tx().forGrakn().execute(insertProductQuery).get();
    }

    static GraqlGet getProductsInContinentQuery(World.Continent continent) {
        return Graql.match(
                Graql.var(CONTINENT)
                        .isa(CONTINENT)
                        .has(LOCATION_NAME, continent.name()),
                Graql.var(PRODUCT)
                        .isa(PRODUCT)
                        .has(PRODUCT_BARCODE, Graql.var(PRODUCT_BARCODE)),
                Graql.var(PRODUCED_IN)
                        .isa(PRODUCED_IN)
                        .rel(PRODUCED_IN_PRODUCT, Graql.var(PRODUCT))
                        .rel(PRODUCED_IN_CONTINENT, Graql.var(CONTINENT))

        ).get();
    }

    @Override
    protected int checkCount() {
//        GraqlGet.Aggregate countQuery = Graql.match(
//
//        ).get().count();
//        return ((Transaction) tx()).count(countQuery);
        return 0;
    }
}