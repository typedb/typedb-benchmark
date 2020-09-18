package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static java.util.Collections.shuffle;

public interface RelocationAgentBase extends InteractionAgent<World.City> {

    @Override
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, IterationContext iterationContext) {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

        LocalDateTime earliestDate;
        if (iterationContext.today().minusYears(2).isBefore(LocalDateTime.of(LocalDate.ofYearDay(0, 1), LocalTime.of(0, 0, 0))))
            earliestDate = iterationContext.today();
        else {
            earliestDate = iterationContext.today().minusYears(2);
        }

        List<String> residentEmails;
        List<String> relocationCityNames;

        int numRelocations = iterationContext.world().getScaleFactor();
        agent.startAction();
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getResidentEmails"))) {
            residentEmails = getResidentEmails(city, earliestDate, numRelocations);
        }
        shuffle(residentEmails, agent.random());

        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getRelocationCityNames"))) {
            relocationCityNames = getRelocationCityNames(city);
        }

        Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
            try (ThreadTrace trace = traceOnThread(agent.checkMethodTrace("insertRelocation"))) {
                insertRelocation(city, iterationContext.today(), residentEmail, relocationCityName);
            }
        });
        agent.commitAction();
        return null;
    }

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate);

    List<String> getResidentEmails(World.City city, LocalDateTime earliestDate, int numRelocations);

    List<String> getRelocationCityNames(World.City city);

    void insertRelocation(World.City city, LocalDateTime today, String email, String newCityName);

}