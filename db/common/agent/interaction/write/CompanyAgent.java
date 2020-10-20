package grakn.simulation.db.common.agent.interaction.write;

import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.agent.region.CountryAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class CompanyAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends CountryAgent<DB_DRIVER, DB_OPERATION> {

    public CompanyAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
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

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {

                for (int i = 0; i < numCompanies; i++) {
                    String adjective = pickOne(simulationContext.world().getAdjectives());
                    String noun = pickOne(simulationContext.world().getNouns());

                    int companyNumber = uniqueId(simulationContext, i);
                    String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
                    runAction(actionFactory().insertCompanyAction(dbOperation, country, simulationContext.today(), companyNumber, companyName));
                }
                dbOperation.save();
            }
        }
    }
}