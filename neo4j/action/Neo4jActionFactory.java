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

package grakn.simulation.neo4j.action;

import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.action.SpouseType;
import grakn.simulation.common.action.insight.ArbitraryOneHopAction;
import grakn.simulation.common.action.read.BirthsInCityAction;
import grakn.simulation.common.action.read.CitiesInContinentAction;
import grakn.simulation.common.action.insight.FindCurrentResidentsAction;
import grakn.simulation.common.action.insight.FindLivedInAction;
import grakn.simulation.common.action.insight.FindSpecificMarriageAction;
import grakn.simulation.common.action.insight.FindSpecificPersonAction;
import grakn.simulation.common.action.insight.FindTransactionCurrencyAction;
import grakn.simulation.common.action.insight.FourHopAction;
import grakn.simulation.common.action.read.MarriedCoupleAction;
import grakn.simulation.common.action.insight.MeanWageOfPeopleInWorldAction;
import grakn.simulation.common.action.read.ProductsInContinentAction;
import grakn.simulation.common.action.insight.ThreeHopAction;
import grakn.simulation.common.action.insight.TwoHopAction;
import grakn.simulation.common.action.read.UnmarriedPeopleInCityAction;
import grakn.simulation.common.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.common.action.write.InsertCompanyAction;
import grakn.simulation.common.action.write.InsertEmploymentAction;
import grakn.simulation.common.action.write.InsertFriendshipAction;
import grakn.simulation.common.action.write.InsertMarriageAction;
import grakn.simulation.common.action.write.InsertParentShipAction;
import grakn.simulation.common.action.write.InsertPersonAction;
import grakn.simulation.common.action.write.InsertProductAction;
import grakn.simulation.common.action.write.InsertRelocationAction;
import grakn.simulation.common.action.write.InsertTransactionAction;
import grakn.simulation.common.utils.Pair;
import grakn.simulation.common.world.World;
import grakn.simulation.neo4j.action.insight.Neo4jArbitraryOneHopAction;
import grakn.simulation.neo4j.action.read.Neo4jBirthsInCityAction;
import grakn.simulation.neo4j.action.read.Neo4jCitiesInContinentAction;
import grakn.simulation.neo4j.action.read.Neo4jCompaniesInCountryAction;
import grakn.simulation.neo4j.action.insight.Neo4jFindCurrentResidentsAction;
import grakn.simulation.neo4j.action.insight.Neo4jFindLivedInAction;
import grakn.simulation.neo4j.action.insight.Neo4jFindSpecificMarriageAction;
import grakn.simulation.neo4j.action.insight.Neo4jFindSpecificPersonAction;
import grakn.simulation.neo4j.action.insight.Neo4jFindTransactionCurrencyAction;
import grakn.simulation.neo4j.action.insight.Neo4jFourHopAction;
import grakn.simulation.neo4j.action.read.Neo4jMarriedCoupleAction;
import grakn.simulation.neo4j.action.insight.Neo4jMeanWageOfPeopleInWorldAction;
import grakn.simulation.neo4j.action.read.Neo4jProductsInContinentAction;
import grakn.simulation.neo4j.action.read.Neo4jResidentsInCityAction;
import grakn.simulation.neo4j.action.insight.Neo4jThreeHopAction;
import grakn.simulation.neo4j.action.insight.Neo4jTwoHopAction;
import grakn.simulation.neo4j.action.read.Neo4jUnmarriedPeopleInCityAction;
import grakn.simulation.neo4j.action.write.Neo4jUpdateAgesOfPeopleInCityAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertCompanyAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertEmploymentAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertFriendshipAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertMarriageAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertParentShipAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertPersonAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertProductAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertRelocationAction;
import grakn.simulation.neo4j.action.write.Neo4jInsertTransactionAction;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Neo4jActionFactory extends ActionFactory<Neo4jOperation, Record> {

    @Override
    public Neo4jResidentsInCityAction residentsInCityAction(Neo4jOperation dbOperation, World.City city, int numResidents, LocalDateTime earliestDate) {
        return new Neo4jResidentsInCityAction(dbOperation, city, numResidents, earliestDate);
    }

    @Override
    public Neo4jCompaniesInCountryAction companiesInCountryAction(Neo4jOperation dbOperation, World.Country country, int numCompanies) {
        return new Neo4jCompaniesInCountryAction(dbOperation, country, numCompanies);
    }

    @Override
    public InsertEmploymentAction<Neo4jOperation, Record> insertEmploymentAction(Neo4jOperation dbOperation, World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        return new Neo4jInsertEmploymentAction(dbOperation, city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    @Override
    public InsertCompanyAction<Neo4jOperation, Record> insertCompanyAction(Neo4jOperation dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        return new Neo4jInsertCompanyAction(dbOperation, country, today, companyNumber, companyName);
    }

    @Override
    public InsertFriendshipAction<Neo4jOperation, Record> insertFriendshipAction(Neo4jOperation dbOperation, LocalDateTime today, String friend1Email, String friend2Email) {
        return new Neo4jInsertFriendshipAction(dbOperation, today, friend1Email, friend2Email);
    }

    @Override
    public UnmarriedPeopleInCityAction<Neo4jOperation> unmarriedPeopleInCityAction(Neo4jOperation dbOperation, World.City city, String gender, LocalDateTime dobOfAdults) {
        return new Neo4jUnmarriedPeopleInCityAction(dbOperation, city, gender, dobOfAdults);
    }

    @Override
    public InsertMarriageAction<Neo4jOperation, Record> insertMarriageAction(Neo4jOperation dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        return new Neo4jInsertMarriageAction(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public BirthsInCityAction<Neo4jOperation> birthsInCityAction(Neo4jOperation dbOperation, World.City city, LocalDateTime today) {
        return new Neo4jBirthsInCityAction(dbOperation, city, today);
    }

    @Override
    public MarriedCoupleAction<Neo4jOperation> marriedCoupleAction(Neo4jOperation dbOperation, World.City city, LocalDateTime today) {
        return new Neo4jMarriedCoupleAction(dbOperation, city, today);
    }

    @Override
    public InsertParentShipAction<Neo4jOperation, Record> insertParentshipAction(Neo4jOperation dbOperation, HashMap<SpouseType, String> marriage, String childEmail) {
        return new Neo4jInsertParentShipAction(dbOperation, marriage, childEmail);
    }

    @Override
    public InsertPersonAction<Neo4jOperation, Record> insertPersonAction(Neo4jOperation dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        return new Neo4jInsertPersonAction(dbOperation, city, today, email, gender, forename, surname);
    }

    @Override
    public InsertProductAction<Neo4jOperation, Record> insertProductAction(Neo4jOperation dbOperation, World.Continent continent, Double barcode, String productName, String productDescription) {
        return new Neo4jInsertProductAction(dbOperation, continent, barcode, productName, productDescription);
    }

    @Override
    public CitiesInContinentAction<Neo4jOperation> citiesInContinentAction(Neo4jOperation dbOperation, World.City city) {
        return new Neo4jCitiesInContinentAction(dbOperation, city);
    }

    @Override
    public InsertRelocationAction<Neo4jOperation, Record> insertRelocationAction(Neo4jOperation dbOperation, World.City city, LocalDateTime today, String residentEmail, String relocationCityName) {
        return new Neo4jInsertRelocationAction(dbOperation, city, today, residentEmail, relocationCityName);
    }

    @Override
    public ProductsInContinentAction<Neo4jOperation> productsInContinentAction(Neo4jOperation dbOperation, World.Continent continent) {
        return new Neo4jProductsInContinentAction(dbOperation, continent);
    }

    @Override
    public InsertTransactionAction<Neo4jOperation, Record> insertTransactionAction(Neo4jOperation dbOperation, World.Country country, Pair<Long, Double> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        return new Neo4jInsertTransactionAction(dbOperation, country, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
    }

    @Override
    public UpdateAgesOfPeopleInCityAction<Neo4jOperation> updateAgesOfPeopleInCityAction(Neo4jOperation dbOperation, LocalDateTime today, World.City city) {
            return new Neo4jUpdateAgesOfPeopleInCityAction(dbOperation, today, city);
    }

    @Override
    public MeanWageOfPeopleInWorldAction<Neo4jOperation> meanWageOfPeopleInWorldAction(Neo4jOperation dbOperation) {
        return new Neo4jMeanWageOfPeopleInWorldAction(dbOperation);
    }

    @Override
    public FindLivedInAction<Neo4jOperation> findlivedInAction(Neo4jOperation dbOperation) {
        return new Neo4jFindLivedInAction(dbOperation);
    }

    @Override
    public FindCurrentResidentsAction<Neo4jOperation> findCurrentResidentsAction(Neo4jOperation dbOperation) {
        return new Neo4jFindCurrentResidentsAction(dbOperation);
    }

    @Override
    public FindTransactionCurrencyAction<Neo4jOperation> findTransactionCurrencyAction(Neo4jOperation dbOperation) {
        return new Neo4jFindTransactionCurrencyAction(dbOperation);
    }

    @Override
    public ArbitraryOneHopAction<Neo4jOperation> arbitraryOneHopAction(Neo4jOperation dbOperation) {
        return new Neo4jArbitraryOneHopAction(dbOperation);
    }

    @Override
    public TwoHopAction<Neo4jOperation> twoHopAction(Neo4jOperation dbOperation) {
        return new Neo4jTwoHopAction(dbOperation);
    }

    @Override
    public ThreeHopAction<Neo4jOperation> threeHopAction(Neo4jOperation dbOperation) {
        return new Neo4jThreeHopAction(dbOperation);
    }

    @Override
    public FourHopAction<Neo4jOperation> fourHopAction(Neo4jOperation dbOperation) {
        return new Neo4jFourHopAction(dbOperation);
    }

    @Override
    public FindSpecificMarriageAction<Neo4jOperation> findSpecificMarriageAction(Neo4jOperation dbOperation) {
        return new Neo4jFindSpecificMarriageAction(dbOperation);
    }

    @Override
    public FindSpecificPersonAction<Neo4jOperation> findSpecificPersonAction(Neo4jOperation dbOperation) {
        return new Neo4jFindSpecificPersonAction(dbOperation);
    }
}
