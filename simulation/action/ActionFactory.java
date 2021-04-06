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

package grakn.benchmark.simulation.action;

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
import grakn.benchmark.simulation.action.read.CompaniesInCountryAction;
import grakn.benchmark.simulation.action.read.MarriedCoupleAction;
import grakn.benchmark.simulation.action.read.ProductsInContinentAction;
import grakn.benchmark.simulation.action.read.ResidentsInCityAction;
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
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;
import grakn.common.collection.Pair;

import java.time.LocalDateTime;
import java.util.HashMap;

public abstract class ActionFactory<TX extends Transaction, DB_RETURN_TYPE> {

    public abstract UpdateAgesOfPeopleInCityAction<TX> updateAgesOfPeopleInCityAction(TX dbOperation, LocalDateTime today, World.City city);

    public abstract ResidentsInCityAction<TX> residentsInCityAction(TX dbOperation, World.City city, int numEmployments, LocalDateTime earliestDate);

    public abstract CompaniesInCountryAction<TX> companiesInCountryAction(TX dbOperation, World.Country country, int numCompanies);

    public abstract InsertEmploymentAction<TX, DB_RETURN_TYPE> insertEmploymentAction(TX dbOperation, World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours);

    public abstract InsertCompanyAction<TX, DB_RETURN_TYPE> insertCompanyAction(TX dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName);

    public abstract InsertFriendshipAction<TX, DB_RETURN_TYPE> insertFriendshipAction(TX dbOperation, LocalDateTime today, String friend1Email, String friend2Email);

    public abstract UnmarriedPeopleInCityAction<TX> unmarriedPeopleInCityAction(TX dbOperation, World.City city, String gender, LocalDateTime dobOfAdults);

    public abstract InsertMarriageAction<TX, DB_RETURN_TYPE> insertMarriageAction(TX dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail);

    public abstract BirthsInCityAction<TX> birthsInCityAction(TX dbOperation, World.City city, LocalDateTime today);

    public abstract MarriedCoupleAction<TX> marriedCoupleAction(TX dbOperation, World.City city, LocalDateTime today);

    public abstract InsertParentShipAction<TX, DB_RETURN_TYPE> insertParentshipAction(TX dbOperation, HashMap<SpouseType, String> marriage, String childEmail);

    public abstract InsertPersonAction<TX, DB_RETURN_TYPE> insertPersonAction(TX dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname);

    public abstract InsertProductAction<TX, DB_RETURN_TYPE> insertProductAction(TX dbOperation, World.Continent continent, Long barcode, String productName, String productDescription);

    public abstract CitiesInContinentAction<TX> citiesInContinentAction(TX dbOperation, World.City city);

    public abstract InsertRelocationAction<TX, DB_RETURN_TYPE> insertRelocationAction(TX dbOperation, World.City city, LocalDateTime today, String residentEmail, String relocationCityName);

    public abstract ProductsInContinentAction<TX> productsInContinentAction(TX dbOperation, World.Continent continent);

    public abstract InsertTransactionAction<TX, DB_RETURN_TYPE> insertTransactionAction(TX dbOperation, World.Country country, Pair<Long, Long> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);

    public abstract MeanWageOfPeopleInWorldAction<TX> meanWageOfPeopleInWorldAction(TX dbOperation);

    public abstract FindLivedInAction<TX> findlivedInAction(TX dbOperation);

    public abstract FindCurrentResidentsAction<TX> findCurrentResidentsAction(TX dbOperation);

    public abstract FindTransactionCurrencyAction<TX> findTransactionCurrencyAction(TX dbOperation);

    public abstract ArbitraryOneHopAction<TX> arbitraryOneHopAction(TX dbOperation);

    public abstract TwoHopAction<TX> twoHopAction(TX dbOperation);

    public abstract ThreeHopAction<TX> threeHopAction(TX dbOperation);

    public abstract FourHopAction<TX> fourHopAction(TX dbOperation);

    public abstract FindSpecificMarriageAction<TX> findSpecificMarriageAction(TX dbOperation);

    public abstract FindSpecificPersonAction<TX> findSpecificPersonAction(TX dbOperation);
}
