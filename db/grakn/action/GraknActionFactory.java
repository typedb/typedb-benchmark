package grakn.simulation.db.grakn.action;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.BirthsInCityAction;
import grakn.simulation.db.common.action.read.CitiesInContinentAction;
import grakn.simulation.db.common.action.read.CompaniesInContinentAction;
import grakn.simulation.db.common.action.read.MarriedCoupleAction;
import grakn.simulation.db.common.action.read.ProductsInContinentAction;
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
import grakn.simulation.db.common.agent.interaction.ParentshipAgent;
import grakn.simulation.db.common.agent.utils.Pair;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.action.read.GraknBirthsInCityAction;
import grakn.simulation.db.grakn.action.read.GraknCitiesInContinentAction;
import grakn.simulation.db.grakn.action.read.GraknCompaniesInContinentAction;
import grakn.simulation.db.grakn.action.read.GraknCompaniesInCountryAction;
import grakn.simulation.db.grakn.action.read.GraknMarriedCoupleAction;
import grakn.simulation.db.grakn.action.read.GraknProductsInContinentAction;
import grakn.simulation.db.grakn.action.read.GraknResidentsInCityAction;
import grakn.simulation.db.grakn.action.read.GraknUnmarriedPeopleInCityAction;
import grakn.simulation.db.grakn.action.read.GraknUpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.grakn.action.write.GraknInsertCompanyAction;
import grakn.simulation.db.grakn.action.write.GraknInsertEmploymentAction;
import grakn.simulation.db.grakn.action.write.GraknInsertFriendshipAction;
import grakn.simulation.db.grakn.action.write.GraknInsertMarriageAction;
import grakn.simulation.db.grakn.action.write.GraknInsertParentShipAction;
import grakn.simulation.db.grakn.action.write.GraknInsertPersonAction;
import grakn.simulation.db.grakn.action.write.GraknInsertProductAction;
import grakn.simulation.db.grakn.action.write.GraknInsertRelocationAction;
import grakn.simulation.db.grakn.action.write.GraknInsertTransactionAction;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;

import java.time.LocalDateTime;
import java.util.HashMap;

public class GraknActionFactory extends ActionFactory<GraknDbOperationController, ConceptMap> {
    public GraknActionFactory(GraknDbOperationController dbOperationController) {
        super(dbOperationController);
    }

    @Override
    public GraknResidentsInCityAction residentsInCityAction(World.City city, int numResidents, LocalDateTime earliestDate) {
        return new GraknResidentsInCityAction(dbOpController.dbOperation(), city, numResidents, earliestDate);
    }

    @Override
    public GraknCompaniesInCountryAction companyNumbersInCountryAction(World.Country country, int numCompanies) {
        return new GraknCompaniesInCountryAction(dbOpController.dbOperation(), country, numCompanies);
    }

    @Override
    public InsertEmploymentAction<?, ConceptMap> insertEmploymentAction(World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        return new GraknInsertEmploymentAction(dbOpController.dbOperation(), city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    @Override
    public InsertCompanyAction<?, ConceptMap> insertCompanyAction(World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        return new GraknInsertCompanyAction(dbOpController.dbOperation(), country, today, companyNumber, companyName);
    }

    @Override
    public InsertFriendshipAction<?, ConceptMap> insertFriendshipAction(LocalDateTime today, String friend1Email, String friend2Email) {
        return new GraknInsertFriendshipAction(dbOpController.dbOperation(), today, friend1Email, friend2Email);
    }

    @Override
    public UnmarriedPeopleInCityAction<?> unmarriedPeopleInCityAction(World.City city, String gender, LocalDateTime dobOfAdults) {
        return new GraknUnmarriedPeopleInCityAction(dbOpController.dbOperation(), city, gender, dobOfAdults);
    }

    @Override
    public InsertMarriageAction<?, ConceptMap> insertMarriageAction(World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        return new GraknInsertMarriageAction(dbOpController.dbOperation(), city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public BirthsInCityAction<?> birthsInCityAction(World.City city, LocalDateTime today) {
        return new GraknBirthsInCityAction(dbOpController.dbOperation(), city, today);
    }

    @Override
    public MarriedCoupleAction<?> marriedCoupleAction(World.City city, LocalDateTime today) {
        return new GraknMarriedCoupleAction(dbOpController.dbOperation(), city, today);
    }

    @Override
    public InsertParentShipAction<?, ConceptMap> insertParentship(HashMap<ParentshipAgent.SpouseType, String> marriage, String childEmail) {
        return new GraknInsertParentShipAction(dbOpController.dbOperation(), marriage, childEmail);
    }

    @Override
    public InsertPersonAction<?, ConceptMap> insertPerson(World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        return new GraknInsertPersonAction(dbOpController.dbOperation(), city, today, email, gender, forename, surname);
    }

    @Override
    public InsertProductAction<?, ConceptMap> insertProduct(World.Continent continent, Double barcode, String productName, String productDescription) {
        return new GraknInsertProductAction(dbOpController.dbOperation(), continent, barcode, productName, productDescription);
    }

    @Override
    public CitiesInContinentAction<?> citiesInContinentAction(World.City city) {
        return new GraknCitiesInContinentAction(dbOpController.dbOperation(), city);
    }

    @Override
    public InsertRelocationAction<?, ConceptMap> insertRelocationAction(World.City city, LocalDateTime today, String residentEmail, String relocationCityName) {
        return new GraknInsertRelocationAction(dbOpController.dbOperation(), city, today, residentEmail, relocationCityName);
    }

    @Override
    public CompaniesInContinentAction<?> companiesInContinentAction(World.Continent continent) {
        return new GraknCompaniesInContinentAction(dbOpController.dbOperation(), continent);
    }

    @Override
    public ProductsInContinentAction<?> productsInContinentAction(World.Continent continent) {
        return new GraknProductsInContinentAction(dbOpController.dbOperation(), continent);
    }

    @Override
    public InsertTransactionAction<?, ConceptMap> insertTransactionAction(World.Continent continent, Pair<Long, Double> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        return new GraknInsertTransactionAction(dbOpController.dbOperation(), continent, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
    }

    @Override
    public UpdateAgesOfPeopleInCityAction<?> updateAgesOfPeopleInCityAction(LocalDateTime today, World.City city) {
        return new GraknUpdateAgesOfPeopleInCityAction(dbOpController.dbOperation(), today, city);
    }
}
