package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.interaction.ProductAgentBase;
import grakn.simulation.db.common.world.World;
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

public abstract class ProductAgent<DB_DRIVER extends DatabaseContext> extends Agent<World.Continent, DB_DRIVER> implements ProductAgentBase {

    @Override
    public void insertProduct(World.Continent continent, Double barcode, String productName, String productDescription) {
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
        tx().execute(insertProductQuery);
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
    public ActionResult resultsForTesting(ConceptMap answer) {
        return null;
    }
}
