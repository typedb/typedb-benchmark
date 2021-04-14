/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.grakn.agent;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.agent.PurchaseAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.common.collection.Pair;

import java.util.List;

import static grakn.benchmark.grakn.agent.Types.COMPANY;
import static grakn.benchmark.grakn.agent.Types.COMPANY_NUMBER;
import static grakn.benchmark.grakn.agent.Types.COUNTRY;
import static grakn.benchmark.grakn.agent.Types.IS_TAXABLE;
import static grakn.benchmark.grakn.agent.Types.LOCATES;
import static grakn.benchmark.grakn.agent.Types.LOCATES_LOCATED;
import static grakn.benchmark.grakn.agent.Types.LOCATES_LOCATION;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.PRODUCT;
import static grakn.benchmark.grakn.agent.Types.PRODUCT_BARCODE;
import static grakn.benchmark.grakn.agent.Types.PRODUCT_QUANTITY;
import static grakn.benchmark.grakn.agent.Types.TRANSACTION;
import static grakn.benchmark.grakn.agent.Types.TRANSACTION_BUYER;
import static grakn.benchmark.grakn.agent.Types.TRANSACTION_MERCHANDISE;
import static grakn.benchmark.grakn.agent.Types.TRANSACTION_SELLER;
import static grakn.benchmark.grakn.agent.Types.VALUE;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknPurchaseAgent extends PurchaseAgent<GraknTransaction> {

    public GraknPurchaseAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<Long> matchCompaniesInCountry(GraknTransaction tx, GeoData.Country country, int numCompanies) {
        return GraknMatcher.matchCompaniesInCountry(tx, country, numCompanies);
    }

    @Override
    protected List<Long> matchProductsInContinent(GraknTransaction tx, GeoData.Continent continent) {
        return GraknMatcher.matchProductsInContinent(tx, continent);
    }

    @Override
    protected void insertPurchase(GraknTransaction tx, GeoData.Country country, Pair<Long, Long> transaction,
                                  Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        tx.execute(match(
                var(PRODUCT).isa(PRODUCT).has(PRODUCT_BARCODE, transaction.second()),
                var("c-buyer").isa(COMPANY).has(COMPANY_NUMBER, transaction.first()),
                var("c-seller").isa(COMPANY).has(COMPANY_NUMBER, sellerCompanyNumber),
                var(COUNTRY).isa(COUNTRY).has(LOCATION_NAME, country.name())
        ).insert(
                // TODO Add currency https://github.com/graknlabs/benchmark/issues/31
                var(TRANSACTION).rel(TRANSACTION_SELLER, var("c-seller"))
                        .rel(TRANSACTION_BUYER, var("c-buyer"))
                        .rel(TRANSACTION_MERCHANDISE, var(PRODUCT))
                        .isa(TRANSACTION)
                        .has(VALUE, value)
                        .has(PRODUCT_QUANTITY, productQuantity)
                        .has(IS_TAXABLE, isTaxable),
                var(LOCATES).rel(LOCATES_LOCATION, var(COUNTRY)).rel(LOCATES_LOCATED, var(TRANSACTION)).isa(LOCATES)
        ));
    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
//        return new HashMap<>() {{
//            put(InsertTransactionActionField.SELLER, tx.getOnlyAttributeOfThing(answer, "c-seller", COMPANY_NUMBER));
//            put(InsertTransactionActionField.BUYER, tx.getOnlyAttributeOfThing(answer, "c-buyer", COMPANY_NUMBER));
//            put(InsertTransactionActionField.MERCHANDISE, tx.getOnlyAttributeOfThing(answer, PRODUCT, PRODUCT_BARCODE));
//            put(InsertTransactionActionField.VALUE, tx.getOnlyAttributeOfThing(answer, TRANSACTION, VALUE));
//            put(InsertTransactionActionField.PRODUCT_QUANTITY, tx.getOnlyAttributeOfThing(answer, TRANSACTION, PRODUCT_QUANTITY));
//            put(InsertTransactionActionField.IS_TAXABLE, tx.getOnlyAttributeOfThing(answer, TRANSACTION, IS_TAXABLE));
//            put(InsertTransactionActionField.COUNTRY, tx.getOnlyAttributeOfThing(answer, COUNTRY, LOCATION_NAME));
//        }};
//    }
}
