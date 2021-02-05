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

package grakn.benchmark.grakn.action.write;

import grakn.benchmark.common.action.write.InsertTransactionAction;
import grakn.benchmark.common.world.World;
import grakn.benchmark.grakn.driver.GraknOperation;
import grakn.client.concept.answer.ConceptMap;
import grakn.common.collection.Pair;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.util.HashMap;

import static grakn.benchmark.grakn.action.Model.COMPANY;
import static grakn.benchmark.grakn.action.Model.COMPANY_NUMBER;
import static grakn.benchmark.grakn.action.Model.COUNTRY;
import static grakn.benchmark.grakn.action.Model.IS_TAXABLE;
import static grakn.benchmark.grakn.action.Model.LOCATES;
import static grakn.benchmark.grakn.action.Model.LOCATES_LOCATED;
import static grakn.benchmark.grakn.action.Model.LOCATES_LOCATION;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PRODUCT;
import static grakn.benchmark.grakn.action.Model.PRODUCT_BARCODE;
import static grakn.benchmark.grakn.action.Model.PRODUCT_QUANTITY;
import static grakn.benchmark.grakn.action.Model.TRANSACTION;
import static grakn.benchmark.grakn.action.Model.TRANSACTION_BUYER;
import static grakn.benchmark.grakn.action.Model.TRANSACTION_MERCHANDISE;
import static grakn.benchmark.grakn.action.Model.TRANSACTION_SELLER;
import static grakn.benchmark.grakn.action.Model.VALUE;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

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
        return match(
                var(PRODUCT)
                        .isa(PRODUCT)
                        .has(PRODUCT_BARCODE, transaction.second()),
                var("c-buyer").isa(COMPANY)
                        .has(COMPANY_NUMBER, transaction.first()),
                var("c-seller").isa(COMPANY)
                        .has(COMPANY_NUMBER, sellerCompanyNumber),
                var(COUNTRY).isa(COUNTRY)
                        .has(LOCATION_NAME, countryName))
                .insert(
                        var(TRANSACTION)
                                .rel(TRANSACTION_SELLER, var("c-seller"))
                                .rel(TRANSACTION_BUYER, var("c-buyer"))
                                .rel(TRANSACTION_MERCHANDISE, var(PRODUCT))
                                .isa(TRANSACTION)
                                //                                .has(CURRENCY)  // TODO Add currency https://github.com/graknlabs/benchmark/issues/31
                                .has(VALUE, value)
                                .has(PRODUCT_QUANTITY, productQuantity)
                                .has(IS_TAXABLE, isTaxable),
                        var(LOCATES)
                                .rel(LOCATES_LOCATION, var(COUNTRY))
                                .rel(LOCATES_LOCATED, var(TRANSACTION))
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
