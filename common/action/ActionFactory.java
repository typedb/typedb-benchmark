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

package grakn.benchmark.common.action;

import grakn.benchmark.common.action.insight.ArbitraryOneHopAction;
import grakn.benchmark.common.action.insight.FindCurrentResidentsAction;
import grakn.benchmark.common.action.insight.FindLivedInAction;
import grakn.benchmark.common.action.insight.FindSpecificMarriageAction;
import grakn.benchmark.common.action.insight.FindSpecificPersonAction;
import grakn.benchmark.common.action.insight.FindTransactionCurrencyAction;
import grakn.benchmark.common.action.insight.FourHopAction;
import grakn.benchmark.common.action.insight.MeanWageOfPeopleInWorldAction;
import grakn.benchmark.common.action.insight.ThreeHopAction;
import grakn.benchmark.common.action.insight.TwoHopAction;
import grakn.benchmark.common.action.read.BirthsInCityAction;
import grakn.benchmark.common.action.read.CitiesInContinentAction;
import grakn.benchmark.common.action.read.CompaniesInCountryAction;
import grakn.benchmark.common.action.read.MarriedCoupleAction;
import grakn.benchmark.common.action.read.ProductsInContinentAction;
import grakn.benchmark.common.action.read.ResidentsInCityAction;
import grakn.benchmark.common.action.read.UnmarriedPeopleInCityAction;
import grakn.benchmark.common.action.write.InsertCompanyAction;
import grakn.benchmark.common.action.write.InsertEmploymentAction;
import grakn.benchmark.common.action.write.InsertFriendshipAction;
import grakn.benchmark.common.action.write.InsertMarriageAction;
import grakn.benchmark.common.action.write.InsertParentShipAction;
import grakn.benchmark.common.action.write.InsertPersonAction;
import grakn.benchmark.common.action.write.InsertProductAction;
import grakn.benchmark.common.action.write.InsertRelocationAction;
import grakn.benchmark.common.action.write.InsertTransactionAction;
import grakn.benchmark.common.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.world.World;
import grakn.common.collection.Pair;

import java.time.LocalDateTime;
import java.util.HashMap;

public abstract class ActionFactory<DB_OPERATION extends DbOperation, DB_RETURN_TYPE> {

    public abstract UpdateAgesOfPeopleInCityAction<DB_OPERATION> updateAgesOfPeopleInCityAction(DB_OPERATION dbOperation, LocalDateTime today, World.City city);

    public abstract ResidentsInCityAction<DB_OPERATION> residentsInCityAction(DB_OPERATION dbOperation, World.City city, int numEmployments, LocalDateTime earliestDate);

    public abstract CompaniesInCountryAction<DB_OPERATION> companiesInCountryAction(DB_OPERATION dbOperation, World.Country country, int numCompanies);

    public abstract InsertEmploymentAction<DB_OPERATION, DB_RETURN_TYPE> insertEmploymentAction(DB_OPERATION dbOperation, World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours);

    public abstract InsertCompanyAction<DB_OPERATION, DB_RETURN_TYPE> insertCompanyAction(DB_OPERATION dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName);

    public abstract InsertFriendshipAction<DB_OPERATION, DB_RETURN_TYPE> insertFriendshipAction(DB_OPERATION dbOperation, LocalDateTime today, String friend1Email, String friend2Email);

    public abstract UnmarriedPeopleInCityAction<DB_OPERATION> unmarriedPeopleInCityAction(DB_OPERATION dbOperation, World.City city, String gender, LocalDateTime dobOfAdults);

    public abstract InsertMarriageAction<DB_OPERATION, DB_RETURN_TYPE> insertMarriageAction(DB_OPERATION dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail);

    public abstract BirthsInCityAction<DB_OPERATION> birthsInCityAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today);

    public abstract MarriedCoupleAction<DB_OPERATION> marriedCoupleAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today);

    public abstract InsertParentShipAction<DB_OPERATION, DB_RETURN_TYPE> insertParentshipAction(DB_OPERATION dbOperation, HashMap<SpouseType, String> marriage, String childEmail);

    public abstract InsertPersonAction<DB_OPERATION, DB_RETURN_TYPE> insertPersonAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname);

    public abstract InsertProductAction<DB_OPERATION, DB_RETURN_TYPE> insertProductAction(DB_OPERATION dbOperation, World.Continent continent, Long barcode, String productName, String productDescription);

    public abstract CitiesInContinentAction<DB_OPERATION> citiesInContinentAction(DB_OPERATION dbOperation, World.City city);

    public abstract InsertRelocationAction<DB_OPERATION, DB_RETURN_TYPE> insertRelocationAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today, String residentEmail, String relocationCityName);

    public abstract ProductsInContinentAction<DB_OPERATION> productsInContinentAction(DB_OPERATION dbOperation, World.Continent continent);

    public abstract InsertTransactionAction<DB_OPERATION, DB_RETURN_TYPE> insertTransactionAction(DB_OPERATION dbOperation, World.Country country, Pair<Long, Long> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);

    public abstract MeanWageOfPeopleInWorldAction<DB_OPERATION> meanWageOfPeopleInWorldAction(DB_OPERATION dbOperation);

    public abstract FindLivedInAction<DB_OPERATION> findlivedInAction(DB_OPERATION dbOperation);

    public abstract FindCurrentResidentsAction<DB_OPERATION> findCurrentResidentsAction(DB_OPERATION dbOperation);

    public abstract FindTransactionCurrencyAction<DB_OPERATION> findTransactionCurrencyAction(DB_OPERATION dbOperation);

    public abstract ArbitraryOneHopAction<DB_OPERATION> arbitraryOneHopAction(DB_OPERATION dbOperation);

    public abstract TwoHopAction<DB_OPERATION> twoHopAction(DB_OPERATION dbOperation);

    public abstract ThreeHopAction<DB_OPERATION> threeHopAction(DB_OPERATION dbOperation);

    public abstract FourHopAction<DB_OPERATION> fourHopAction(DB_OPERATION dbOperation);

    public abstract FindSpecificMarriageAction<DB_OPERATION> findSpecificMarriageAction(DB_OPERATION dbOperation);

    public abstract FindSpecificPersonAction<DB_OPERATION> findSpecificPersonAction(DB_OPERATION dbOperation);
}
