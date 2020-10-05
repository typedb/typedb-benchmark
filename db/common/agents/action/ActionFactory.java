package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;

public abstract class ActionFactory<DB_OP_CONTROLLER extends DbOperationController, DB_RETURN_TYPE> {

    protected DB_OP_CONTROLLER dbOpController;

    public ActionFactory(DB_OP_CONTROLLER dbOpController) {
        this.dbOpController = dbOpController;
    }

    public abstract ResidentsInCityAction<?> residentsInCityAction(World.City city, int numEmployments, LocalDateTime earliestDate);

    public abstract CompanyNumbersAction<?> companyNumbersInCountryAction(World.Country country, int numCompanies);

    public abstract InsertEmploymentAction<?, DB_RETURN_TYPE> insertEmploymentAction(World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours);

    public abstract UpdateAgesOfPeopleInCityAction<?> updateAgesOfPeopleInCityAction(LocalDateTime today, World.City city);
}
