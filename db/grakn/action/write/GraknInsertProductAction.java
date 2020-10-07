package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.write.InsertProductAction;
import grakn.simulation.db.common.driver.GraknOperation;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.util.HashMap;

import static grakn.simulation.db.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN_CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCED_IN_PRODUCT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_BARCODE;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_DESCRIPTION;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_NAME;

public class GraknInsertProductAction extends InsertProductAction<GraknOperation, ConceptMap> {
    public GraknInsertProductAction(GraknOperation dbOperation, World.Continent continent, Double barcode, String productName, String productDescription) {
        super(dbOperation, continent, barcode, productName, productDescription);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert insertProductQuery = Graql.match(
                Graql.var(CONTINENT)
                        .isa(CONTINENT)
                        .has(LOCATION_NAME, continent.name())
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
        return singleResult(dbOperation.execute(insertProductQuery));
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return null;
    }
}
