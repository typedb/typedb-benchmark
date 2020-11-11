package grakn.simulation.common.agent.write;

import grakn.simulation.common.agent.base.SimulationContext;
import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.agent.region.CountryAgent;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.world.World;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class CompanyAgent<DB_OPERATION extends DbOperation> extends CountryAgent<DB_OPERATION> {

    public CompanyAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
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
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.Country country, SimulationContext simulationContext) {
            int numCompanies = simulationContext.world().getScaleFactor();

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {

                for (int i = 0; i < numCompanies; i++) {
                    String adjective = pickOne(simulationContext.world().getAdjectives());
                    String noun = pickOne(simulationContext.world().getNouns());

                    int companyNumber = uniqueId(simulationContext, i).hashCode();
                    String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
                    runAction(actionFactory().insertCompanyAction(dbOperation, country, simulationContext.today(), companyNumber, companyName));
                }
                dbOperation.save();
            }
        }
    }
}