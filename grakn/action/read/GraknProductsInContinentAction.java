package grakn.simulation.grakn.action.read;

import grakn.simulation.common.action.read.ProductsInContinentAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.grakn.schema.Schema.PRODUCED_IN;
import static grakn.simulation.grakn.schema.Schema.PRODUCED_IN_CONTINENT;
import static grakn.simulation.grakn.schema.Schema.PRODUCED_IN_PRODUCT;
import static grakn.simulation.grakn.schema.Schema.PRODUCT;
import static grakn.simulation.grakn.schema.Schema.PRODUCT_BARCODE;

public class GraknProductsInContinentAction extends ProductsInContinentAction<GraknOperation> {

    public GraknProductsInContinentAction(GraknOperation dbOperation, World.Continent continent) {
        super(dbOperation, continent);
    }

    @Override
    public List<Double> run() {
        GraqlGet.Unfiltered query = query(continent.name());
        return dbOperation.sortedExecute(query, PRODUCT_BARCODE, null);
    }

    public static GraqlGet.Unfiltered query(String continentName) {
        return Graql.match(
                    Graql.var(CONTINENT)
                            .isa(CONTINENT)
                            .has(LOCATION_NAME, continentName),
                    Graql.var(PRODUCT)
                            .isa(PRODUCT)
                            .has(PRODUCT_BARCODE, Graql.var(PRODUCT_BARCODE)),
                    Graql.var(PRODUCED_IN)
                            .isa(PRODUCED_IN)
                            .rel(PRODUCED_IN_PRODUCT, Graql.var(PRODUCT))
                            .rel(PRODUCED_IN_CONTINENT, Graql.var(CONTINENT))

            ).get();
    }
}
