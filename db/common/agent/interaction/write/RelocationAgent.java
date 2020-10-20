package grakn.simulation.db.common.agent.interaction.write;

import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.CitiesInContinentAction;
import grakn.simulation.db.common.action.read.ResidentsInCityAction;
import grakn.simulation.db.common.agent.region.CityAgent;
import grakn.simulation.db.common.agent.utils.Allocation;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class RelocationAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends CityAgent<DB_DRIVER, DB_OPERATION> {

    public RelocationAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected RegionalRelocationAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalRelocationAgent(simulationStep, tracker, random, test);
    }

    public class RegionalRelocationAgent extends RegionalAgent {
        public RegionalRelocationAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city, SimulationContext simulationContext) {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

            LocalDateTime earliestDateOfResidencyToRelocate;
            earliestDateOfResidencyToRelocate = simulationContext.today().minusYears(2);

            List<String> residentEmails;
            List<String> relocationCityNames;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                ResidentsInCityAction<?> residentsInCityAction = actionFactory().residentsInCityAction(dbOperation, city, simulationContext.world().getScaleFactor(), earliestDateOfResidencyToRelocate);
                residentEmails = runAction(residentsInCityAction);
            }
            shuffle(residentEmails);

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                CitiesInContinentAction<?> citiesInContinentAction = actionFactory().citiesInContinentAction(dbOperation, city);
                relocationCityNames = runAction(citiesInContinentAction);
            }

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
                    runAction(actionFactory().insertRelocationAction(dbOperation, city, simulationContext.today(), residentEmail, relocationCityName));
                });
                dbOperation.save();
            }
        }
    }
}