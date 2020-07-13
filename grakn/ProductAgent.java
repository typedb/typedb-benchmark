package grakn.simulation.grakn;

import grakn.simulation.world.World;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

public class ProductAgent extends grakn.simulation.agents.interaction.ProductAgent {

    @Override
    protected void insertProduct(int iterationScopeId) {
        GraqlInsert insertProductQuery = Graql.match(
                Graql.var("continent")
                        .isa("continent")
                        .has("location-name", continent().name())
        ).insert(
                Graql.var("product")
                        .isa("product")
                        .has("product-barcode", Double.valueOf(uniqueId(iterationScopeId)))
                        .has("product-name", randomAttributeGenerator().boundRandomLengthRandomString(5, 20))
                        .has("product-description", randomAttributeGenerator().boundRandomLengthRandomString(75, 100)),
                Graql.var("prod")
                        .isa("produced-in")
                        .rel("produced-in_product", Graql.var("product"))
                        .rel("produced-in_continent", Graql.var("continent"))
                );
        log().query("insertProduct", insertProductQuery);
        tx().forGrakn().execute(insertProductQuery);
    }

    static GraqlGet.Unfiltered getProductsInContinentQuery(World.Continent continent) {
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