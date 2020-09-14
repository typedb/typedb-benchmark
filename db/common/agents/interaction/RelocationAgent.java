package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.agents.region.CityAgent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface RelocationAgent extends InteractionAgent<World.City> {

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

        agent.startAction();
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getResidentEmails"))) {
            residentEmails = getResidentEmails(earliestDate);
        }
        shuffle(residentEmails, agent.random());

        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getRelocationCityNames"))) {
            relocationCityNames = getRelocationCityNames();
        }

        Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
            try (ThreadTrace trace = traceOnThread(agent.checkMethodTrace("insertRelocation"))) {
                insertRelocation(residentEmail, relocationCityName);
            }
        });
        agent.commitAction();
        return null;
    }

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate);

    abstract protected List<String> getResidentEmails(LocalDateTime earliestDate);

    abstract protected List<String> getRelocationCityNames();

    abstract protected void insertRelocation(String email, String newCityName);

}