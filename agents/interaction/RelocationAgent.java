package grakn.simulation.agents.interaction;

import grakn.simulation.agents.world.CityAgent;
import grakn.simulation.common.Allocation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

        residentEmails = getResidentEmails(earliestDate);
        shuffle(residentEmails);

        relocationCityNames = getRelocationCityNames();

        Allocation.allocate(residentEmails, relocationCityNames, this::insertRelocation);

        tx().commit();
    }

//    TODO Should this be abstracted?
//    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate);

    abstract protected List<String> getResidentEmails(LocalDateTime earliestDate);

    abstract protected List<String> getRelocationCityNames();

    abstract protected void insertRelocation(String email, String newCityName);
}