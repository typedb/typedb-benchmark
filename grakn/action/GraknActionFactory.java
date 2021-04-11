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

package grakn.benchmark.grakn.action;

import grakn.benchmark.grakn.action.insight.GraknArbitraryOneHopAction;
import grakn.benchmark.grakn.action.insight.GraknFindCurrentResidentsAction;
import grakn.benchmark.grakn.action.insight.GraknFindLivedInAction;
import grakn.benchmark.grakn.action.insight.GraknFindSpecificMarriageAction;
import grakn.benchmark.grakn.action.insight.GraknFindSpecificPersonAction;
import grakn.benchmark.grakn.action.insight.GraknFindTransactionCurrencyAction;
import grakn.benchmark.grakn.action.insight.GraknFourHopAction;
import grakn.benchmark.grakn.action.insight.GraknMeanWageOfPeopleInWorldAction;
import grakn.benchmark.grakn.action.insight.GraknThreeHopAction;
import grakn.benchmark.grakn.action.insight.GraknTwoHopAction;
import grakn.benchmark.grakn.action.read.GraknBirthsInCityAction;
import grakn.benchmark.grakn.action.read.GraknCitiesInContinentAction;
import grakn.benchmark.grakn.action.read.GraknCompaniesInCountryAction;
import grakn.benchmark.grakn.action.read.GraknMarriedCoupleAction;
import grakn.benchmark.grakn.action.read.GraknProductsInContinentAction;
import grakn.benchmark.grakn.action.read.GraknResidentsInCityAction;
import grakn.benchmark.grakn.action.read.GraknUnmarriedPeopleInCityAction;
import grakn.benchmark.grakn.action.write.GraknInsertCompanyAction;
import grakn.benchmark.grakn.action.write.GraknInsertEmploymentAction;
import grakn.benchmark.grakn.action.write.GraknInsertFriendshipAction;
import grakn.benchmark.grakn.action.write.GraknInsertMarriageAction;
import grakn.benchmark.grakn.action.write.GraknInsertParentShipAction;
import grakn.benchmark.grakn.action.write.GraknInsertPersonAction;
import grakn.benchmark.grakn.action.write.GraknInsertProductAction;
import grakn.benchmark.grakn.action.write.GraknInsertRelocationAction;
import grakn.benchmark.grakn.action.write.GraknInsertTransactionAction;
import grakn.benchmark.grakn.action.write.GraknUpdateAgesOfPeopleInCityAction;
import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.action.SpouseType;
import grakn.benchmark.simulation.action.insight.ArbitraryOneHopAction;
import grakn.benchmark.simulation.action.insight.FindCurrentResidentsAction;
import grakn.benchmark.simulation.action.insight.FindLivedInAction;
import grakn.benchmark.simulation.action.insight.FindSpecificMarriageAction;
import grakn.benchmark.simulation.action.insight.FindSpecificPersonAction;
import grakn.benchmark.simulation.action.insight.FindTransactionCurrencyAction;
import grakn.benchmark.simulation.action.insight.FourHopAction;
import grakn.benchmark.simulation.action.insight.MeanWageOfPeopleInWorldAction;
import grakn.benchmark.simulation.action.insight.ThreeHopAction;
import grakn.benchmark.simulation.action.insight.TwoHopAction;
import grakn.benchmark.simulation.action.read.BirthsInCityAction;
import grakn.benchmark.simulation.action.read.CitiesInContinentAction;
import grakn.benchmark.simulation.action.read.MarriedCoupleAction;
import grakn.benchmark.simulation.action.read.ProductsInContinentAction;
import grakn.benchmark.simulation.action.read.UnmarriedPeopleInCityAction;
import grakn.benchmark.simulation.action.write.InsertCompanyAction;
import grakn.benchmark.simulation.action.write.InsertEmploymentAction;
import grakn.benchmark.simulation.action.write.InsertFriendshipAction;
import grakn.benchmark.simulation.action.write.InsertMarriageAction;
import grakn.benchmark.simulation.action.write.InsertParentShipAction;
import grakn.benchmark.simulation.action.write.InsertPersonAction;
import grakn.benchmark.simulation.action.write.InsertProductAction;
import grakn.benchmark.simulation.action.write.InsertRelocationAction;
import grakn.benchmark.simulation.action.write.InsertTransactionAction;
import grakn.benchmark.simulation.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.benchmark.simulation.common.GeoData;
import grakn.client.api.answer.ConceptMap;
import grakn.common.collection.Pair;

import java.time.LocalDateTime;
import java.util.HashMap;

public class GraknActionFactory extends ActionFactory<GraknTransaction, ConceptMap> {

    @Override
    public GraknResidentsInCityAction residentsInCityAction(GraknTransaction tx, GeoData.City city, int numResidents, LocalDateTime earliestDate) {
        return new GraknResidentsInCityAction(tx, city, numResidents, earliestDate);
    }

    @Override
    public GraknCompaniesInCountryAction companiesInCountryAction(GraknTransaction tx, GeoData.Country country, int numCompanies) {
        return new GraknCompaniesInCountryAction(tx, country, numCompanies);
    }

    @Override
    public InsertEmploymentAction<GraknTransaction, ConceptMap> insertEmploymentAction(GraknTransaction tx, GeoData.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        return new GraknInsertEmploymentAction(tx, city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    @Override
    public InsertCompanyAction<GraknTransaction, ConceptMap> insertCompanyAction(GraknTransaction tx, GeoData.Country country, LocalDateTime today, int companyNumber, String companyName) {
        return new GraknInsertCompanyAction(tx, country, today, companyNumber, companyName);
    }

    @Override
    public InsertFriendshipAction<GraknTransaction, ConceptMap> insertFriendshipAction(GraknTransaction tx, LocalDateTime today, String friend1Email, String friend2Email) {
        return new GraknInsertFriendshipAction(tx, today, friend1Email, friend2Email);
    }

    @Override
    public UnmarriedPeopleInCityAction<GraknTransaction> unmarriedPeopleInCityAction(GraknTransaction tx, GeoData.City city, String gender, LocalDateTime dobOfAdults) {
        return new GraknUnmarriedPeopleInCityAction(tx, city, gender, dobOfAdults);
    }

    @Override
    public InsertMarriageAction<GraknTransaction, ConceptMap> insertMarriageAction(GraknTransaction tx, GeoData.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        return new GraknInsertMarriageAction(tx, city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public BirthsInCityAction<GraknTransaction> birthsInCityAction(GraknTransaction tx, GeoData.City city, LocalDateTime today) {
        return new GraknBirthsInCityAction(tx, city, today);
    }

    @Override
    public MarriedCoupleAction<GraknTransaction> marriedCoupleAction(GraknTransaction tx, GeoData.City city, LocalDateTime today) {
        return new GraknMarriedCoupleAction(tx, city, today);
    }

    @Override
    public InsertParentShipAction<GraknTransaction, ConceptMap> insertParentshipAction(GraknTransaction tx, HashMap<SpouseType, String> marriage, String childEmail) {
        return new GraknInsertParentShipAction(tx, marriage, childEmail);
    }

    @Override
    public InsertPersonAction<GraknTransaction, ConceptMap> insertPersonAction(GraknTransaction tx, GeoData.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        return new GraknInsertPersonAction(tx, city, today, email, gender, forename, surname);
    }

    @Override
    public InsertProductAction<GraknTransaction, ConceptMap> insertProductAction(GraknTransaction tx, GeoData.Continent continent, Long barcode, String productName, String productDescription) {
        return new GraknInsertProductAction(tx, continent, barcode, productName, productDescription);
    }

    @Override
    public CitiesInContinentAction<GraknTransaction> citiesInContinentAction(GraknTransaction tx, GeoData.City city) {
        return new GraknCitiesInContinentAction(tx, city);
    }

    @Override
    public InsertRelocationAction<GraknTransaction, ConceptMap> insertRelocationAction(GraknTransaction tx, GeoData.City city, LocalDateTime today, String residentEmail, String relocationCityName) {
        return new GraknInsertRelocationAction(tx, city, today, residentEmail, relocationCityName);
    }

    @Override
    public ProductsInContinentAction<GraknTransaction> productsInContinentAction(GraknTransaction tx, GeoData.Continent continent) {
        return new GraknProductsInContinentAction(tx, continent);
    }

    @Override
    public InsertTransactionAction<GraknTransaction, ConceptMap> insertTransactionAction(GraknTransaction tx, GeoData.Country country, Pair<Long, Long> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        return new GraknInsertTransactionAction(tx, country, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
    }

    @Override
    public UpdateAgesOfPeopleInCityAction<GraknTransaction> updateAgesOfPeopleInCityAction(GraknTransaction tx, LocalDateTime today, GeoData.City city) {
        return new GraknUpdateAgesOfPeopleInCityAction(tx, today, city);
    }

    @Override
    public MeanWageOfPeopleInWorldAction<GraknTransaction> meanWageOfPeopleInWorldAction(GraknTransaction tx) {
        return new GraknMeanWageOfPeopleInWorldAction(tx);
    }

    @Override
    public FindLivedInAction<GraknTransaction> findlivedInAction(GraknTransaction tx) {
        return new GraknFindLivedInAction(tx);
    }

    @Override
    public FindCurrentResidentsAction<GraknTransaction> findCurrentResidentsAction(GraknTransaction tx) {
        return new GraknFindCurrentResidentsAction(tx);
    }

    @Override
    public FindTransactionCurrencyAction<GraknTransaction> findTransactionCurrencyAction(GraknTransaction tx) {
        return new GraknFindTransactionCurrencyAction(tx);
    }

    @Override
    public ArbitraryOneHopAction<GraknTransaction> arbitraryOneHopAction(GraknTransaction tx) {
        return new GraknArbitraryOneHopAction(tx);
    }

    @Override
    public TwoHopAction<GraknTransaction> twoHopAction(GraknTransaction tx) {
        return new GraknTwoHopAction(tx);
    }

    @Override
    public ThreeHopAction<GraknTransaction> threeHopAction(GraknTransaction tx) {
        return new GraknThreeHopAction(tx);
    }

    @Override
    public FourHopAction<GraknTransaction> fourHopAction(GraknTransaction tx) {
        return new GraknFourHopAction(tx);
    }

    @Override
    public FindSpecificMarriageAction<GraknTransaction> findSpecificMarriageAction(GraknTransaction tx) {
        return new GraknFindSpecificMarriageAction(tx);
    }

    @Override
    public FindSpecificPersonAction<GraknTransaction> findSpecificPersonAction(GraknTransaction tx) {
        return new GraknFindSpecificPersonAction(tx);
    }
}
