package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.SimulationContext;
import grakn.simulation.db.common.agent.region.CountryAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.world.World;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class CompanyAgent<DB_DRIVER extends DbDriver> extends CountryAgent<DB_DRIVER> {

    public CompanyAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected RegionalCompanyAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalCompanyAgent(simulationStep, tracker, random, test);
    }

    public class RegionalCompanyAgent extends RegionalAgent {
        public RegionalCompanyAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationController dbOperationController, World.Country country, SimulationContext simulationContext) {
            int numCompanies = simulationContext.world().getScaleFactor();

            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("InsertEmploymentAction", tracker())) {

                for (int i = 0; i < numCompanies; i++) {
                    String adjective = pickOne(simulationContext.world().getAdjectives());
                    String noun = pickOne(simulationContext.world().getNouns());

                    int companyNumber = uniqueId(simulationContext, i);
                    String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
                    runAction(dbOperationController.actionFactory().insertCompanyAction(country, simulationContext.today(), companyNumber, companyName));
                }
                dbOperation.save();
            }
        }
    }
}