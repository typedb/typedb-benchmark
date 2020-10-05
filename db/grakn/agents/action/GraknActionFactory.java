package grakn.simulation.db.grakn.agents.action;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.action.ActionFactory;
import grakn.simulation.db.common.agents.action.InsertEmploymentAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.agents.interaction.GraknDbOperationController;

import java.time.LocalDateTime;

public class GraknActionFactory extends ActionFactory<GraknDbOperationController, ConceptMap> {
    public GraknActionFactory(GraknDbOperationController dbOperationController) {
        super(dbOperationController);
    }

    @Override
    public GraknResidentsInCityAction residentsInCityAction(World.City city, int numResidents, LocalDateTime earliestDate) {
        return new GraknResidentsInCityAction(dbOpController.dbOperation(), city, numResidents, earliestDate);
    }

    @Override
    public GraknCompanyNumbersAction companyNumbersInCountryAction(World.Country country, int numCompanies) {
        return new GraknCompanyNumbersAction(dbOpController.dbOperation(), country, numCompanies);
    }

    @Override
    public InsertEmploymentAction<?, ConceptMap> insertEmploymentAction(World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        return new GraknInsertEmploymentAction(dbOpController.dbOperation(), city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }
}
