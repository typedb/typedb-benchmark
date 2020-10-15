package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.CompanyNumbersAction;
import grakn.simulation.db.common.action.read.ResidentsInCityAction;
import grakn.simulation.db.common.agent.region.CityAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static grakn.simulation.db.common.agent.utils.Allocation.allocate;

public class EmploymentAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends CityAgent<DB_DRIVER, DB_OPERATION> {

    public EmploymentAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
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
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city, SimulationContext simulationContext) {
            LocalDateTime employmentDate = simulationContext.today().minusYears(0);
            List<String> employeeEmails;
            List<Long> companyNumbers;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                ResidentsInCityAction<DB_OPERATION> employeeEmailsAction = actionFactory().residentsInCityAction(dbOperation, city, simulationContext.world().getScaleFactor(), employmentDate);
                employeeEmails = runAction(employeeEmailsAction);
            }

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                CompanyNumbersAction<DB_OPERATION> companyNumbersAction = actionFactory().companyNumbersInCountryAction(dbOperation, city.country(), simulationContext.world().getScaleFactor());
                companyNumbers = runAction(companyNumbersAction);
            }

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                // A second transaction is being used to circumvent graknlabs/grakn issue #5585
                boolean allocated = allocate(employeeEmails, companyNumbers, (employeeEmail, companyNumber) -> {
                    double wageValue = randomAttributeGenerator().boundRandomDouble(MIN_ANNUAL_WAGE, MAX_ANNUAL_WAGE);
                    String contractContent = randomAttributeGenerator().boundRandomLengthRandomString(MIN_CONTRACT_CHARACTER_LENGTH, MAX_CONTRACT_CHARACTER_LENGTH);
                    double contractedHours = randomAttributeGenerator().boundRandomDouble(MIN_CONTRACTED_HOURS, MAX_CONTRACTED_HOURS);
                    runAction(actionFactory().insertEmploymentAction(dbOperation, city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours));
                });
                if (allocated) {
                    dbOperation.save();
                }
            }
        }
    }
}
