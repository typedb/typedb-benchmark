package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.agents.world.CityAgent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class RelocationAgent extends CityAgent {

    @Override
    public final void iterate() {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

        LocalDateTime earliestDate;
        if (today().minusYears(2).isBefore(LocalDateTime.of(LocalDate.ofYearDay(0, 1), LocalTime.of(0, 0, 0))))
            earliestDate = today();
        else {
            earliestDate = today().minusYears(2);
        }

        List<String> residentEmails;
        List<String> relocationCityNames;

        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getResidentEmails"))) {
            residentEmails = getResidentEmails(earliestDate);
        }
        shuffle(residentEmails);

        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getRelocationCityNames"))) {
            relocationCityNames = getRelocationCityNames();
        }

        Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
            try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertRelocation"))) {
                insertRelocation(residentEmail, relocationCityName);
            }
        });

        tx().commitWithTracing();
    }

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate);

    abstract protected List<String> getResidentEmails(LocalDateTime earliestDate);

    abstract protected List<String> getRelocationCityNames();

    abstract protected void insertRelocation(String email, String newCityName);
}