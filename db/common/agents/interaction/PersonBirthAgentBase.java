package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.base.ActionResultList;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface PersonBirthAgentBase extends Agent.RegionalAgent<World.City> {

    enum PersonBirthAgentField implements DbOperationController.ComparableField {
        EMAIL, GENDER, FORENAME, SURNAME, DATE_OF_BIRTH
    }

    @Override
    default void iterate(DbOperationController<World.City, ?> agent, World.City city, SimulationContext simulationContext) {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        int numBirths = simulationContext.world().getScaleFactor();
        ActionResultList agentResultSet = new ActionResultList();
        for (int i = 0; i < numBirths; i++) {
            String gender;
            String forename;
            String surname = agent.pickOne(simulationContext.world().getSurnames());

            boolean genderBool = agent.random().nextBoolean();
            if (genderBool) {
                gender = "male";
                forename = agent.pickOne(simulationContext.world().getMaleForenames());
            } else {
                gender = "female";
                forename = agent.pickOne(simulationContext.world().getFemaleForenames());
            }

            // Email is used as a key and needs to be unique, which requires a lot of information
            String email = forename + "."
                    + surname + "_"
                    + simulationContext.today().toString() + "_"
                    + i + "_"
                    + simulationContext.simulationStep() + "_"
                    + city + "_"
                    + city.country() + "_"
                    + city.country().continent()
                    + "@gmail.com";
            agent.startDbOperation("insertPerson", tracker);
            try (ThreadTrace trace = traceOnThread(agent.action())) {
                agentResultSet.add(insertPerson(city, simulationContext.today(), email, gender, forename, surname));
            }
        }
        agent.saveDbOperation();
        return agentResultSet;
    }

    ActionResult insertPerson(World.City city, LocalDateTime today, String email, String gender, String forename, String surname);
}
