package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.action.ResidentsInCityAction;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.region.CityAgent;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.context.DbDriver;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class RelocationAgent<DB_DRIVER extends DbDriver> extends CityAgent<DB_DRIVER> {

    public RelocationAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
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
        protected void run(DbOperationController dbOperationController, World.City city, SimulationContext simulationContext) {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

            LocalDateTime earliestDateOfResidencyToRelocate;
            earliestDateOfResidencyToRelocate = simulationContext.today().minusYears(2);

            List<String> residentEmails;
            List<String> relocationCityNames;

            ResidentsInCityAction<?> residentsInCityAction = dbOperationController.actionFactory().residentsInCityAction(city, simulationContext.world().getScaleFactor(), earliestDateOfResidencyToRelocate);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(residentsInCityAction, tracker())) {
                residentEmails = runAction(residentsInCityAction);
            }
            shuffle(residentEmails);

            CitiesInContinentAction citiesInContinentAction = dbOperationController.actionFactory().citiesInContinentAction(city);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(citiesInContinentAction, tracker())) {
                relocationCityNames = runAction(citiesInContinentAction);
            }

            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("InsertRelocation", tracker())) {
                Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
                    runAction(dbOperationController.actionFactory().insertRelocationAction(city, simulationContext.today(), residentEmail, relocationCityName));
                });
                dbOperation.save();
            }
        }

        enum RelocationAgentField implements DbOperationController.ComparableField {
            PERSON_EMAIL, OLD_CITY_NAME, NEW_CITY_NAME, RELOCATION_DATE,
            RESIDENT_EMAILS, RELOCATION_CITY_NAMES
        }
    }
}