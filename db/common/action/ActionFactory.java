package grakn.simulation.db.common.action;

import grakn.simulation.db.common.action.read.BirthsInCityAction;
import grakn.simulation.db.common.action.read.CitiesInContinentAction;
import grakn.simulation.db.common.action.read.CompaniesInContinentAction;
import grakn.simulation.db.common.action.read.CompanyNumbersAction;
import grakn.simulation.db.common.action.read.MarriedCoupleAction;
import grakn.simulation.db.common.action.read.ProductsInContinentAction;
import grakn.simulation.db.common.action.read.ResidentsInCityAction;
import grakn.simulation.db.common.action.read.UnmarriedPeopleInCityAction;
import grakn.simulation.db.common.action.read.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.common.action.write.InsertCompanyAction;
import grakn.simulation.db.common.action.write.InsertEmploymentAction;
import grakn.simulation.db.common.action.write.InsertFriendshipAction;
import grakn.simulation.db.common.action.write.InsertMarriageAction;
import grakn.simulation.db.common.action.write.InsertParentShipAction;
import grakn.simulation.db.common.action.write.InsertPersonAction;
import grakn.simulation.db.common.action.write.InsertProductAction;
import grakn.simulation.db.common.action.write.InsertRelocationAction;
import grakn.simulation.db.common.action.write.InsertTransactionAction;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.utils.Pair;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.HashMap;

public abstract class ActionFactory<DB_OPERATION extends DbOperation, DB_RETURN_TYPE> {

    public abstract UpdateAgesOfPeopleInCityAction<DB_OPERATION> updateAgesOfPeopleInCityAction(DB_OPERATION dbOperation, LocalDateTime today, World.City city);

    public abstract ResidentsInCityAction<DB_OPERATION> residentsInCityAction(DB_OPERATION dbOperation, World.City city, int numEmployments, LocalDateTime earliestDate);

    public abstract CompanyNumbersAction<DB_OPERATION> companyNumbersInCountryAction(DB_OPERATION dbOperation, World.Country country, int numCompanies);

    public abstract InsertEmploymentAction<DB_OPERATION, DB_RETURN_TYPE> insertEmploymentAction(DB_OPERATION dbOperation, World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours);

    public abstract InsertCompanyAction<DB_OPERATION, DB_RETURN_TYPE> insertCompanyAction(DB_OPERATION dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName);

    public abstract InsertFriendshipAction<DB_OPERATION, DB_RETURN_TYPE> insertFriendshipAction(DB_OPERATION dbOperation, LocalDateTime today, String friend1Email, String friend2Email);

    public abstract UnmarriedPeopleInCityAction<DB_OPERATION> unmarriedPeopleInCityAction(DB_OPERATION dbOperation, World.City city, String gender, LocalDateTime dobOfAdults);

    public abstract InsertMarriageAction<DB_OPERATION, DB_RETURN_TYPE> insertMarriageAction(DB_OPERATION dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail);

    public abstract BirthsInCityAction<DB_OPERATION> birthsInCityAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today);

    public abstract MarriedCoupleAction<DB_OPERATION> marriedCoupleAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today);

    public abstract InsertParentShipAction<DB_OPERATION, DB_RETURN_TYPE> insertParentshipAction(DB_OPERATION dbOperation, HashMap<SpouseType, String> marriage, String childEmail);

    public abstract InsertPersonAction<DB_OPERATION, DB_RETURN_TYPE> insertPersonAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname);

    public abstract InsertProductAction<DB_OPERATION, DB_RETURN_TYPE> insertProductAction(DB_OPERATION dbOperation, World.Continent continent, Double barcode, String productName, String productDescription);

    public abstract CitiesInContinentAction<DB_OPERATION> citiesInContinentAction(DB_OPERATION dbOperation, World.City city);

    public abstract InsertRelocationAction<DB_OPERATION, DB_RETURN_TYPE> insertRelocationAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today, String residentEmail, String relocationCityName);

    public abstract CompaniesInContinentAction<DB_OPERATION> companiesInContinentAction(DB_OPERATION dbOperation, World.Continent continent);

    public abstract ProductsInContinentAction<DB_OPERATION> productsInContinentAction(DB_OPERATION dbOperation, World.Continent continent);

    public abstract InsertTransactionAction<DB_OPERATION, DB_RETURN_TYPE> insertTransactionAction(DB_OPERATION dbOperation, World.Continent continent, Pair<Long, Double> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);
}
