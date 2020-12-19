/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.simulation.grakn.action.write;

import grakn.client.concept.answer.ConceptMap;
import grakn.simulation.common.action.write.InsertTransactionAction;
import grakn.simulation.common.utils.Pair;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.util.HashMap;

import static grakn.simulation.grakn.action.Model.COMPANY;
import static grakn.simulation.grakn.action.Model.COMPANY_NUMBER;
import static grakn.simulation.grakn.action.Model.COUNTRY;
import static grakn.simulation.grakn.action.Model.IS_TAXABLE;
import static grakn.simulation.grakn.action.Model.LOCATES;
import static grakn.simulation.grakn.action.Model.LOCATES_LOCATED;
import static grakn.simulation.grakn.action.Model.LOCATES_LOCATION;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PRODUCT;
import static grakn.simulation.grakn.action.Model.PRODUCT_BARCODE;
import static grakn.simulation.grakn.action.Model.PRODUCT_QUANTITY;
import static grakn.simulation.grakn.action.Model.TRANSACTION;
import static grakn.simulation.grakn.action.Model.TRANSACTION_BUYER;
import static grakn.simulation.grakn.action.Model.TRANSACTION_MERCHANDISE;
import static grakn.simulation.grakn.action.Model.TRANSACTION_SELLER;
import static grakn.simulation.grakn.action.Model.VALUE;

public class GraknInsertTransactionAction extends InsertTransactionAction<GraknOperation, ConceptMap> {

    public GraknInsertTransactionAction(GraknOperation dbOperation, World.Country country, Pair<Long, Long> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        super(dbOperation, country, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert insertTransactionQuery = query(transaction, sellerCompanyNumber, country.name(), value, productQuantity, isTaxable);
        return singleResult(dbOperation.execute(insertTransactionQuery));
    }

    public static GraqlInsert query(Pair<Long, Long> transaction, Long sellerCompanyNumber, String countryName, double value, int productQuantity, boolean isTaxable) {
        return Graql.match(
                    Graql.var(PRODUCT)
                            .isa(PRODUCT)
                            .has(PRODUCT_BARCODE, transaction.getSecond()),
                    Graql.var("c-buyer").isa(COMPANY)
                            .has(COMPANY_NUMBER, transaction.getFirst()),
                    Graql.var("c-seller").isa(COMPANY)
                            .has(COMPANY_NUMBER, sellerCompanyNumber),
                    Graql.var(COUNTRY).isa(COUNTRY)
                            .has(LOCATION_NAME, countryName))
                    .insert(
                            Graql.var(TRANSACTION)
                                    .rel(TRANSACTION_SELLER, Graql.var("c-seller"))
                                    .rel(TRANSACTION_BUYER, Graql.var("c-buyer"))
                                    .rel(TRANSACTION_MERCHANDISE, Graql.var(PRODUCT))
                                    .isa(TRANSACTION)
    //                                .has(CURRENCY)  // TODO Add currency https://github.com/graknlabs/simulation/issues/31
                                    .has(VALUE, value)
                                    .has(PRODUCT_QUANTITY, productQuantity)
                                    .has(IS_TAXABLE, isTaxable),
                            Graql.var(LOCATES)
                                    .rel(LOCATES_LOCATION, Graql.var(COUNTRY))
                                    .rel(LOCATES_LOCATED, Graql.var(TRANSACTION))
                                    .isa(LOCATES)
                    );
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
            put(InsertTransactionActionField.COUNTRY, dbOperation.getOnlyAttributeOfThing(answer, COUNTRY, LOCATION_NAME));
        }};
    }
}
