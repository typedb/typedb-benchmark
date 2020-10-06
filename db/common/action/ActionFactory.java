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
import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.agent.interaction.ParentshipAgent;
import grakn.simulation.db.common.agent.utils.Pair;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.HashMap;

public abstract class ActionFactory<DB_OP_CONTROLLER extends DbOperationController, DB_RETURN_TYPE> {

    protected DB_OP_CONTROLLER dbOpController;

    public ActionFactory(DB_OP_CONTROLLER dbOpController) {
        this.dbOpController = dbOpController;
    }

    public abstract UpdateAgesOfPeopleInCityAction<?> updateAgesOfPeopleInCityAction(LocalDateTime today, World.City city);

    public abstract ResidentsInCityAction<?> residentsInCityAction(World.City city, int numEmployments, LocalDateTime earliestDate);

    public abstract CompanyNumbersAction<?> companyNumbersInCountryAction(World.Country country, int numCompanies);

    public abstract InsertEmploymentAction<?, DB_RETURN_TYPE> insertEmploymentAction(World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours);

    public abstract InsertCompanyAction<?, DB_RETURN_TYPE> insertCompanyAction(World.Country country, LocalDateTime today, int companyNumber, String companyName);

    public abstract InsertFriendshipAction<?, DB_RETURN_TYPE> insertFriendshipAction(LocalDateTime today, String friend1Email, String friend2Email);

    public abstract UnmarriedPeopleInCityAction<?> unmarriedPeopleInCityAction(World.City city, String gender, LocalDateTime dobOfAdults);

    public abstract InsertMarriageAction<?, DB_RETURN_TYPE> insertMarriageAction(World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail);

    public abstract BirthsInCityAction<?> birthsInCityAction(World.City city, LocalDateTime today);

    public abstract MarriedCoupleAction<?> marriedCoupleAction(World.City city, LocalDateTime today);

    public abstract InsertParentShipAction<?, DB_RETURN_TYPE> insertParentship(HashMap<ParentshipAgent.SpouseType, String> marriage, String childEmail);

    public abstract InsertPersonAction<?, DB_RETURN_TYPE> insertPerson(World.City city, LocalDateTime today, String email, String gender, String forename, String surname);

    public abstract InsertProductAction<?, DB_RETURN_TYPE> insertProduct(World.Continent continent, Double barcode, String productName, String productDescription);

    public abstract CitiesInContinentAction<?> citiesInContinentAction(World.City city);

    public abstract InsertRelocationAction<?, DB_RETURN_TYPE> insertRelocationAction(World.City city, LocalDateTime today, String residentEmail, String relocationCityName);

    public abstract CompaniesInContinentAction<?> companiesInContinentAction(World.Continent continent);

    public abstract ProductsInContinentAction<?> productsInContinentAction(World.Continent continent);

    public abstract InsertTransactionAction<?, DB_RETURN_TYPE> insertTransactionAction(World.Continent continent, Pair<Long, Double> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);
}
