package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.world.World;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

public class ProductAgent extends grakn.simulation.db.common.agents.interaction.ProductAgent {

    @Override
    protected void insertProduct(Double barcode, String productName, String productDescription) {
        GraqlInsert insertProductQuery = Graql.match(
                Graql.var("continent")
                        .isa("continent")
                        .has("location-name", continent().name())
        ).insert(
                Graql.var("product")
                        .isa("product")
                        .has("product-barcode", barcode)
                        .has("product-name", productName)
                        .has("product-description", productDescription),
                Graql.var("prod")
                        .isa("produced-in")
                        .rel("produced-in_product", Graql.var("product"))
                        .rel("produced-in_continent", Graql.var("continent"))
                );
        log().query("insertProduct", insertProductQuery);
        tx().forGrakn().execute(insertProductQuery);
    }

    static GraqlGet getProductsInContinentQuery(World.Continent continent) {
        return Graql.match(
                Graql.var("continent")
                        .isa("continent")
                        .has("location-name", continent.name()),
                Graql.var("product")
                        .isa("product")
                        .has("product-barcode", Graql.var("product-barcode")),
                Graql.var("prod")
                        .isa("produced-in")
                        .rel("produced-in_product", Graql.var("product"))
                        .rel("produced-in_continent", Graql.var("continent"))

        ).get();
    }
}