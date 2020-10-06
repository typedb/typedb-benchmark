package grakn.simulation.db.grakn.action.read;

import grakn.simulation.db.common.action.read.ProductsInContinentAction;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN_CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN_PRODUCT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_BARCODE;

public class GraknProductsInContinentAction extends ProductsInContinentAction<GraknDbOperationController.TransactionalDbOperation> {

    public GraknProductsInContinentAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, World.Continent continent) {
        super(dbOperation, continent);
    }

    @Override
    public List<Double> run() {
        GraqlGet.Unfiltered query = Graql.match(
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
        return dbOperation.tx().getOrderedAttribute(query, PRODUCT_BARCODE, null);
    }
}
