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
import grakn.benchmark.grakn.driver.GraknOperation;
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
import grakn.benchmark.simulation.world.World;
import grakn.client.api.answer.ConceptMap;
import grakn.common.collection.Pair;

import java.time.LocalDateTime;
import java.util.HashMap;

public class GraknActionFactory extends ActionFactory<GraknOperation, ConceptMap> {

    @Override
    public GraknResidentsInCityAction residentsInCityAction(GraknOperation dbOperation, World.City city, int numResidents, LocalDateTime earliestDate) {
        return new GraknResidentsInCityAction(dbOperation, city, numResidents, earliestDate);
    }

    @Override
    public GraknCompaniesInCountryAction companiesInCountryAction(GraknOperation dbOperation, World.Country country, int numCompanies) {
        return new GraknCompaniesInCountryAction(dbOperation, country, numCompanies);
    }

    @Override
    public InsertEmploymentAction<GraknOperation, ConceptMap> insertEmploymentAction(GraknOperation dbOperation, World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        return new GraknInsertEmploymentAction(dbOperation, city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    @Override
    public InsertCompanyAction<GraknOperation, ConceptMap> insertCompanyAction(GraknOperation dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        return new GraknInsertCompanyAction(dbOperation, country, today, companyNumber, companyName);
    }

    @Override
    public InsertFriendshipAction<GraknOperation, ConceptMap> insertFriendshipAction(GraknOperation dbOperation, LocalDateTime today, String friend1Email, String friend2Email) {
        return new GraknInsertFriendshipAction(dbOperation, today, friend1Email, friend2Email);
    }

    @Override
    public UnmarriedPeopleInCityAction<GraknOperation> unmarriedPeopleInCityAction(GraknOperation dbOperation, World.City city, String gender, LocalDateTime dobOfAdults) {
        return new GraknUnmarriedPeopleInCityAction(dbOperation, city, gender, dobOfAdults);
    }

    @Override
    public InsertMarriageAction<GraknOperation, ConceptMap> insertMarriageAction(GraknOperation dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        return new GraknInsertMarriageAction(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public BirthsInCityAction<GraknOperation> birthsInCityAction(GraknOperation dbOperation, World.City city, LocalDateTime today) {
        return new GraknBirthsInCityAction(dbOperation, city, today);
    }

    @Override
    public MarriedCoupleAction<GraknOperation> marriedCoupleAction(GraknOperation dbOperation, World.City city, LocalDateTime today) {
        return new GraknMarriedCoupleAction(dbOperation, city, today);
    }

    @Override
    public InsertParentShipAction<GraknOperation, ConceptMap> insertParentshipAction(GraknOperation dbOperation, HashMap<SpouseType, String> marriage, String childEmail) {
        return new GraknInsertParentShipAction(dbOperation, marriage, childEmail);
    }

    @Override
    public InsertPersonAction<GraknOperation, ConceptMap> insertPersonAction(GraknOperation dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        return new GraknInsertPersonAction(dbOperation, city, today, email, gender, forename, surname);
    }

    @Override
    public InsertProductAction<GraknOperation, ConceptMap> insertProductAction(GraknOperation dbOperation, World.Continent continent, Long barcode, String productName, String productDescription) {
        return new GraknInsertProductAction(dbOperation, continent, barcode, productName, productDescription);
    }

    @Override
    public CitiesInContinentAction<GraknOperation> citiesInContinentAction(GraknOperation dbOperation, World.City city) {
        return new GraknCitiesInContinentAction(dbOperation, city);
    }

    @Override
    public InsertRelocationAction<GraknOperation, ConceptMap> insertRelocationAction(GraknOperation dbOperation, World.City city, LocalDateTime today, String residentEmail, String relocationCityName) {
        return new GraknInsertRelocationAction(dbOperation, city, today, residentEmail, relocationCityName);
    }

    @Override
    public ProductsInContinentAction<GraknOperation> productsInContinentAction(GraknOperation dbOperation, World.Continent continent) {
        return new GraknProductsInContinentAction(dbOperation, continent);
    }

    @Override
    public InsertTransactionAction<GraknOperation, ConceptMap> insertTransactionAction(GraknOperation dbOperation, World.Country country, Pair<Long, Long> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        return new GraknInsertTransactionAction(dbOperation, country, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
    }

    @Override
    public UpdateAgesOfPeopleInCityAction<GraknOperation> updateAgesOfPeopleInCityAction(GraknOperation dbOperation, LocalDateTime today, World.City city) {
        return new GraknUpdateAgesOfPeopleInCityAction(dbOperation, today, city);
    }

    @Override
    public MeanWageOfPeopleInWorldAction<GraknOperation> meanWageOfPeopleInWorldAction(GraknOperation dbOperation) {
        return new GraknMeanWageOfPeopleInWorldAction(dbOperation);
    }

    @Override
    public FindLivedInAction<GraknOperation> findlivedInAction(GraknOperation dbOperation) {
        return new GraknFindLivedInAction(dbOperation);
    }

    @Override
    public FindCurrentResidentsAction<GraknOperation> findCurrentResidentsAction(GraknOperation dbOperation) {
        return new GraknFindCurrentResidentsAction(dbOperation);
    }

    @Override
    public FindTransactionCurrencyAction<GraknOperation> findTransactionCurrencyAction(GraknOperation dbOperation) {
        return new GraknFindTransactionCurrencyAction(dbOperation);
    }

    @Override
    public ArbitraryOneHopAction<GraknOperation> arbitraryOneHopAction(GraknOperation dbOperation) {
        return new GraknArbitraryOneHopAction(dbOperation);
    }

    @Override
    public TwoHopAction<GraknOperation> twoHopAction(GraknOperation dbOperation) {
        return new GraknTwoHopAction(dbOperation);
    }

    @Override
    public ThreeHopAction<GraknOperation> threeHopAction(GraknOperation dbOperation) {
        return new GraknThreeHopAction(dbOperation);
    }

    @Override
    public FourHopAction<GraknOperation> fourHopAction(GraknOperation dbOperation) {
        return new GraknFourHopAction(dbOperation);
    }

    @Override
    public FindSpecificMarriageAction<GraknOperation> findSpecificMarriageAction(GraknOperation dbOperation) {
        return new GraknFindSpecificMarriageAction(dbOperation);
    }

    @Override
    public FindSpecificPersonAction<GraknOperation> findSpecificPersonAction(GraknOperation dbOperation) {
        return new GraknFindSpecificPersonAction(dbOperation);
    }
}
