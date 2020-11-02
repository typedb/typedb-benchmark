package grakn.simulation.db.grakn.action;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.SpouseType;
import grakn.simulation.db.common.action.read.BirthsInCityAction;
import grakn.simulation.db.common.action.read.CitiesInContinentAction;
import grakn.simulation.db.common.action.read.CompaniesInContinentAction;
import grakn.simulation.db.common.action.insight.FindCurrentResidentsAction;
import grakn.simulation.db.common.action.insight.FindLivedInAction;
import grakn.simulation.db.common.action.insight.FindSpecificMarriageAction;
import grakn.simulation.db.common.action.insight.FindSpecificPersonAction;
import grakn.simulation.db.common.action.insight.FindTransactionCurrencyAction;
import grakn.simulation.db.common.action.insight.FourHopAction;
import grakn.simulation.db.common.action.insight.ArbitraryOneHopAction;
import grakn.simulation.db.common.action.read.MarriedCoupleAction;
import grakn.simulation.db.common.action.insight.MeanWageOfPeopleInWorldAction;
import grakn.simulation.db.common.action.read.ProductsInContinentAction;
import grakn.simulation.db.common.action.insight.ThreeHopAction;
import grakn.simulation.db.common.action.insight.TwoHopAction;
import grakn.simulation.db.common.action.read.UnmarriedPeopleInCityAction;
import grakn.simulation.db.common.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.common.action.write.InsertCompanyAction;
import grakn.simulation.db.common.action.write.InsertEmploymentAction;
import grakn.simulation.db.common.action.write.InsertFriendshipAction;
import grakn.simulation.db.common.action.write.InsertMarriageAction;
import grakn.simulation.db.common.action.write.InsertParentShipAction;
import grakn.simulation.db.common.action.write.InsertPersonAction;
import grakn.simulation.db.common.action.write.InsertProductAction;
import grakn.simulation.db.common.action.write.InsertRelocationAction;
import grakn.simulation.db.common.action.write.InsertTransactionAction;
import grakn.simulation.db.common.utils.Pair;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.action.read.GraknBirthsInCityAction;
import grakn.simulation.db.grakn.action.read.GraknCitiesInContinentAction;
import grakn.simulation.db.grakn.action.read.GraknCompaniesInContinentAction;
import grakn.simulation.db.grakn.action.read.GraknCompaniesInCountryAction;
import grakn.simulation.db.grakn.action.insight.GraknFindCurrentResidentsAction;
import grakn.simulation.db.grakn.action.insight.GraknFindLivedInAction;
import grakn.simulation.db.grakn.action.insight.GraknFindSpecificMarriageAction;
import grakn.simulation.db.grakn.action.insight.GraknFindSpecificPersonAction;
import grakn.simulation.db.grakn.action.insight.GraknFindTransactionCurrencyAction;
import grakn.simulation.db.grakn.action.insight.GraknFourHopAction;
import grakn.simulation.db.grakn.action.insight.GraknArbitraryOneHopAction;
import grakn.simulation.db.grakn.action.read.GraknMarriedCoupleAction;
import grakn.simulation.db.grakn.action.insight.GraknMeanWageOfPeopleInWorldAction;
import grakn.simulation.db.grakn.action.read.GraknProductsInContinentAction;
import grakn.simulation.db.grakn.action.read.GraknResidentsInCityAction;
import grakn.simulation.db.grakn.action.insight.GraknThreeHopAction;
import grakn.simulation.db.grakn.action.insight.GraknTwoHopAction;
import grakn.simulation.db.grakn.action.read.GraknUnmarriedPeopleInCityAction;
import grakn.simulation.db.grakn.action.write.GraknUpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.grakn.action.write.GraknInsertCompanyAction;
import grakn.simulation.db.grakn.action.write.GraknInsertEmploymentAction;
import grakn.simulation.db.grakn.action.write.GraknInsertFriendshipAction;
import grakn.simulation.db.grakn.action.write.GraknInsertMarriageAction;
import grakn.simulation.db.grakn.action.write.GraknInsertParentShipAction;
import grakn.simulation.db.grakn.action.write.GraknInsertPersonAction;
import grakn.simulation.db.grakn.action.write.GraknInsertProductAction;
import grakn.simulation.db.grakn.action.write.GraknInsertRelocationAction;
import grakn.simulation.db.grakn.action.write.GraknInsertTransactionAction;
import grakn.simulation.db.grakn.driver.GraknOperation;

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
    public InsertProductAction<GraknOperation, ConceptMap> insertProductAction(GraknOperation dbOperation, World.Continent continent, Double barcode, String productName, String productDescription) {
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
    public CompaniesInContinentAction<GraknOperation> companiesInContinentAction(GraknOperation dbOperation, World.Continent continent) {
        return new GraknCompaniesInContinentAction(dbOperation, continent);
    }

    @Override
    public ProductsInContinentAction<GraknOperation> productsInContinentAction(GraknOperation dbOperation, World.Continent continent) {
        return new GraknProductsInContinentAction(dbOperation, continent);
    }

    @Override
    public InsertTransactionAction<GraknOperation, ConceptMap> insertTransactionAction(GraknOperation dbOperation, World.Continent continent, Pair<Long, Double> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        return new GraknInsertTransactionAction(dbOperation, continent, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
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
