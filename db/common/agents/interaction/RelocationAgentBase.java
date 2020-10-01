package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.base.ActionResultList;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.agents.interaction.RelocationAgentBase.RelocationAgentField.RELOCATION_CITY_NAMES;
import static grakn.simulation.db.common.agents.interaction.RelocationAgentBase.RelocationAgentField.RESIDENT_EMAILS;
import static java.util.Collections.shuffle;

public interface RelocationAgentBase extends InteractionAgent<World.City> {

    enum RelocationAgentField implements Agent.ComparableField {
        PERSON_EMAIL, OLD_CITY_NAME, NEW_CITY_NAME, RELOCATION_DATE,
        RESIDENT_EMAILS, RELOCATION_CITY_NAMES
    }

    @Override
    default void iterate(Agent<World.City, ?> agent, World.City city, SimulationContext simulationContext) {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

        LocalDateTime earliestDateOfResidencyToRelocate;
        earliestDateOfResidencyToRelocate = simulationContext.today().minusYears(2);

        List<String> residentEmails;
        List<String> relocationCityNames;

        int numRelocations = simulationContext.world().getScaleFactor();
        agent.startDbOperation("getResidentEmails");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            residentEmails = getResidentEmails(city, earliestDateOfResidencyToRelocate, numRelocations);
        }
        shuffle(residentEmails, agent.random());

        ActionResultList agentResultSet = new ActionResultList();
        agentResultSet.add(new ActionResult(){{
            put(RESIDENT_EMAILS, residentEmails);
        }});

        agent.startDbOperation("getRelocationCityNames");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            relocationCityNames = getRelocationCityNames(city);
        }
        agentResultSet.add(new ActionResult(){{
            put(RELOCATION_CITY_NAMES, relocationCityNames);
        }});

        Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
            agent.startDbOperation("insertRelocation");
            try (ThreadTrace trace = traceOnThread(agent.action())) {
                agentResultSet.add(insertRelocation(city, simulationContext.today(), residentEmail, relocationCityName));
            }
        });
        agent.saveDbOperation();
        return agentResultSet;
    }

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate);

    List<String> getResidentEmails(World.City city, LocalDateTime earliestDate, int numRelocations);

    List<String> getRelocationCityNames(World.City city);

    ActionResult insertRelocation(World.City city, LocalDateTime today, String email, String newCityName);

}