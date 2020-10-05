package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.action.CompanyNumbersAction;
import grakn.simulation.db.common.agents.action.ResidentsInCityAction;
import grakn.simulation.db.common.agents.action.InsertEmploymentAction;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.region.CityAgent;
import grakn.simulation.db.common.context.DbDriver;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static grakn.simulation.db.common.agents.utils.Allocation.allocate;

public class EmploymentAgent<DB_DRIVER extends DbDriver> extends CityAgent<DB_DRIVER> {

    public EmploymentAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected RegionalEmploymentAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalEmploymentAgent(simulationStep, tracker, random, test);
    }

    public class RegionalEmploymentAgent extends RegionalAgent {
        double MIN_ANNUAL_WAGE = 18000.00;
        double MAX_ANNUAL_WAGE = 80000.00;
        double MIN_CONTRACTED_HOURS = 30.0;
        double MAX_CONTRACTED_HOURS = 70.0;
        int MIN_CONTRACT_CHARACTER_LENGTH = 200;
        int MAX_CONTRACT_CHARACTER_LENGTH = 600;

        public RegionalEmploymentAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationController dbOperationController, World.City city, SimulationContext simulationContext) {
            LocalDateTime employmentDate = simulationContext.today().minusYears(0);
            List<String> employeeEmails;
            List<Long> companyNumbers;

            ResidentsInCityAction<?> employeeEmailsAction = dbOperationController.actionFactory().residentsInCityAction(city, simulationContext.world().getScaleFactor(), employmentDate);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(employeeEmailsAction, tracker())) {
                employeeEmails = runAction(employeeEmailsAction);
            }

            CompanyNumbersAction<?> companyNumbersAction = dbOperationController.actionFactory().companyNumbersInCountryAction(city.country(), simulationContext.world().getScaleFactor());
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(companyNumbersAction, tracker())) {
                companyNumbers = runAction(companyNumbersAction);
            }

            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("InsertEmploymentAction", tracker())) {
                // A second transaction is being used to circumvent graknlabs/grakn issue #5585
                boolean allocated = allocate(employeeEmails, companyNumbers, (employeeEmail, companyNumber) -> {
                    double wageValue = randomAttributeGenerator().boundRandomDouble(MIN_ANNUAL_WAGE, MAX_ANNUAL_WAGE);
                    String contractContent = randomAttributeGenerator().boundRandomLengthRandomString(MIN_CONTRACT_CHARACTER_LENGTH, MAX_CONTRACT_CHARACTER_LENGTH);
                    double contractedHours = randomAttributeGenerator().boundRandomDouble(MIN_CONTRACTED_HOURS, MAX_CONTRACTED_HOURS);
                    runAction(dbOperationController.actionFactory().insertEmploymentAction(city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours));
                });
                if (allocated) {
                    dbOperation.save();
                }
            }
        }
    }
}
