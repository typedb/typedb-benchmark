package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.write.InsertTransactionAction;
import grakn.simulation.db.common.driver.GraknOperation;
import grakn.simulation.db.common.utils.Pair;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.util.HashMap;

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

public class GraknInsertTransactionAction extends InsertTransactionAction<GraknOperation, ConceptMap> {

    public GraknInsertTransactionAction(GraknOperation dbOperation, World.Continent continent, Pair<Long, Double> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        super(dbOperation, continent, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
    }

    @Override
    public ConceptMap run() {
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
        return singleResult(dbOperation.execute(insertTransactionQuery));
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertTransactionActionField.SELLER, dbOperation.getOnlyAttributeOfThing(answer, "c-seller", COMPANY_NUMBER));
            put(InsertTransactionActionField.BUYER, dbOperation.getOnlyAttributeOfThing(answer, "c-buyer", COMPANY_NUMBER));
            put(InsertTransactionActionField.MERCHANDISE, dbOperation.getOnlyAttributeOfThing(answer, PRODUCT, PRODUCT_BARCODE));
            put(InsertTransactionActionField.VALUE, dbOperation.getOnlyAttributeOfThing(answer, TRANSACTION, VALUE));
            put(InsertTransactionActionField.PRODUCT_QUANTITY, dbOperation.getOnlyAttributeOfThing(answer, TRANSACTION, PRODUCT_QUANTITY));
            put(InsertTransactionActionField.IS_TAXABLE, dbOperation.getOnlyAttributeOfThing(answer, TRANSACTION, IS_TAXABLE));
            put(InsertTransactionActionField.CONTINENT, dbOperation.getOnlyAttributeOfThing(answer, CONTINENT, LOCATION_NAME));
        }};
    }
}
