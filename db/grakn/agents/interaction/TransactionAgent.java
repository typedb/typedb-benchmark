package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.interaction.TransactionAgentBase;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.world.World;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.util.List;

import static grakn.simulation.db.grakn.agents.interaction.CompanyAgent.getCompanyNumbersInContinentQuery;
import static grakn.simulation.db.grakn.agents.interaction.ProductAgent.getProductsInContinentQuery;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.IS_TAXABLE;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATED;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_BARCODE;
import static grakn.simulation.db.grakn.schema.Schema.PRODUCT_QUANTITY;
import static grakn.simulation.db.grakn.schema.Schema.TRANSACTION;
import static grakn.simulation.db.grakn.schema.Schema.TRANSACTION_BUYER;
import static grakn.simulation.db.grakn.schema.Schema.TRANSACTION_MERCHANDISE;
import static grakn.simulation.db.grakn.schema.Schema.TRANSACTION_SELLER;
import static grakn.simulation.db.grakn.schema.Schema.VALUE;

public abstract class TransactionAgent<DB_DRIVER extends DatabaseContext> extends Agent<World.Continent, DB_DRIVER> implements TransactionAgentBase {

    @Override
    public List<Long> getCompanyNumbersInContinent(World.Continent continent){
        return tx().getOrderedAttribute(getCompanyNumbersInContinentQuery(continent), COMPANY_NUMBER, null);
    }

    @Override
    public List<Double> getProductBarcodesInContinent(World.Continent continent) {
        return tx().getOrderedAttribute(getProductsInContinentQuery(continent), PRODUCT_BARCODE, null);
    }

    @Override
    public void insertTransaction(World.Continent continent, Pair<Long, Double> transaction, long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable){
        GraqlInsert insertTransactionQuery = Graql.match(
                Graql.var(PRODUCT)
                        .isa(PRODUCT)
                        .has(PRODUCT_BARCODE, transaction.getSecond()),
                Graql.var("c-buyer").isa(COMPANY)
                        .has(COMPANY_NUMBER, transaction.getFirst()),
                Graql.var("c-seller").isa(COMPANY)
                        .has(COMPANY_NUMBER, sellerCompanyNumber),
                Graql.var(CONTINENT).isa(CONTINENT)
                        .has(LOCATION_NAME, continent.name()))
                .insert(
                        Graql.var(TRANSACTION)
                                .isa(TRANSACTION)
                                .rel(TRANSACTION_SELLER, Graql.var("c-seller"))
                                .rel(TRANSACTION_BUYER, Graql.var("c-buyer"))
                                .rel(TRANSACTION_MERCHANDISE, Graql.var(PRODUCT))
//                                .has(CURRENCY)  // TODO Add currency https://github.com/graknlabs/simulation/issues/31
                                .has(VALUE, value)
                                .has(PRODUCT_QUANTITY, productQuantity)
                                .has(IS_TAXABLE, isTaxable),
                        Graql.var(LOCATES)
                                .isa(LOCATES)
                                .rel(LOCATES_LOCATION, Graql.var(CONTINENT))
                                .rel(LOCATES_LOCATED, Graql.var(TRANSACTION))

                );
        tx().execute(insertTransactionQuery);
    }

    @Override
    public ActionResult resultsForTesting(ConceptMap answer) {
        return null;
    }
}
