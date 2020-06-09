package grakn.simulation.agents;

import grakn.simulation.agents.common.CityAgent;
import grakn.simulation.common.Allocation;
import grakn.simulation.common.ExecutorUtils;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static grakn.simulation.common.ExecutorUtils.getOrderedAttribute;

public class RelocationAgent extends CityAgent {

    @Override
    public void iterate() {
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

    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate) {
        return Graql.match(
                Graql.var("person").isa("person").has("email", Graql.var("email")),
                Graql.var("city").isa("city").has("location-name", city.name()),
                Graql.var("r").isa("residency")
                        .rel("residency_resident", "person")
                        .rel("residency_location", "city")
                        .has("start-date", Graql.var("start-date")),
                Graql.not(Graql.var("r").has("end-date", Graql.var("ed"))),
                Graql.var("start-date").lte(earliestDate)
        ).get();
    }

    private List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet.Unfiltered cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        int numRelocations = world().getScaleFactor();
        return ExecutorUtils.getOrderedAttribute(tx(), cityResidentsQuery, "email", numRelocations);
    }

    private List<String> getRelocationCityNames() {

        GraqlGet.Unfiltered relocationCitiesQuery = Graql.match(
                Graql.var("city").isa("city").has("location-name", Graql.var("city-name")),
                Graql.var("continent").isa("continent").has("location-name", city().country().continent().name()),
                Graql.var("lh1").isa("location-hierarchy").rel("city").rel("continent"),
                Graql.var("city-name").neq(city().name())
        ).get();

        log().query("getRelocationCityNames", relocationCitiesQuery);
        return getOrderedAttribute(tx(), relocationCitiesQuery, "city-name");
    }

    private void insertRelocation(String email, String newCityName) {
        GraqlInsert relocatePersonQuery = Graql.match(
                Graql.var("p").isa("person").has("email", email),
                Graql.var("new-city").isa("city").has("location-name", newCityName),
                Graql.var("old-city").isa("city").has("location-name", city().name())
        ).insert(
                Graql.var("r").isa("relocation")
                        .rel("relocation_previous-location", "old-city")
                        .rel("relocation_new-location", "new-city")
                        .rel("relocation_relocated-person", "p")
                        .has("relocation-date", today())
        );

        log().query("insertRelocation", relocatePersonQuery);
        tx().execute(relocatePersonQuery);
    }
}