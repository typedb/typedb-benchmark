package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.region.CountryAgent;
import grakn.simulation.db.common.context.DbDriver;
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
                    InsertCompanyAction insertCompanyAction = dbOperationController.actionFactory().insertCompanyAction(country, simulationContext.today(), companyNumber, companyName);
                    runAction(insertCompanyAction);
                }
                dbOperation.save();
            }
        }
    }

    enum CompanyAgentField implements DbOperationController.ComparableField {
        COMPANY_NUMBER, COMPANY_NAME, DATE_OF_INCORPORATION, COUNTRY
    }
}