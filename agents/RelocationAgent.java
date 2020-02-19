package grakn.simulation.agents;

import grakn.simulation.common.Allocation;
import grakn.simulation.common.LogWrapper;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class RelocationAgent extends CityAgent {

    private static final LogWrapper<World.City> LOG = new LogWrapper<>(LoggerFactory.getLogger(PersonBirthAgent.class), World.City::getTracker);
    private static final int NUM_RELOCATIONS = 5;

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

        if (relocationCityNames.size() > 0 && residentEmails.size() > 0) {

            List<Integer> cityAllocationPerPerson = Allocation.allocateEvenly(residentEmails.size(), relocationCityNames.size());

            for (int i = 0; i < cityAllocationPerPerson.size(); i++) {
                String email = residentEmails.get(i);
                String newCityName = relocationCityNames.get(cityAllocationPerPerson.get(i));

                insertRelocation(email, newCityName);
            }
            tx().commit();
        }
    }

    private List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet.Unfiltered cityResidentsQuery = Graql.match(
                Graql.var("person").isa("person").has("email", Graql.var("email")),
                Graql.var("city").isa("city").has("name", city().getName()),
                Graql.var("r").isa("residency")
                        .rel("residency_resident", "person")
                        .rel("residency_location", "city")
                        .has("start-date", Graql.var("start-date")),
                Graql.not(Graql.var("r").has("end-date", Graql.var("ed"))),
                Graql.var("start-date").lte(earliestDate)
        ).get();

        LOG.query(city(), "getResidentEmails", cityResidentsQuery);
        return tx().execute(cityResidentsQuery).stream()
                .map(conceptMap -> conceptMap.get("email").asAttribute().value().toString())
                .sorted()
                .limit(NUM_RELOCATIONS)
                .collect(toList());
    }

    private List<String> getRelocationCityNames() {

        GraqlGet.Unfiltered relocationCitiesQuery = Graql.match(
                Graql.var("city").isa("city").has("name", Graql.var("city-name")),
                Graql.var("continent").isa("continent").has("name", city().getCountry().getContinent().getName()),
                Graql.var("lh1").isa("location-hierarchy").rel("city").rel("continent"),
                Graql.var("city-name").neq(city().getName())
        ).get();

        LOG.query(city(), "getRelocationCityNames", relocationCitiesQuery);
        return tx().execute(relocationCitiesQuery)
                .stream()
                .map(conceptMap -> conceptMap.get("city-name").asAttribute().value().toString())
                .collect(toList());
    }

    private void insertRelocation(String email, String newCityName) {
        GraqlInsert relocatePersonQuery = Graql.match(
                Graql.var("p").isa("person").has("email", email),
                Graql.var("new-city").isa("city").has("name", newCityName),
                Graql.var("old-city").isa("city").has("name", city().getName())
        ).insert(
                Graql.var("r").isa("relocation")
                        .rel("relocation_previous-location", "old-city")
                        .rel("relocation_new-location", "new-city")
                        .rel("relocation_relocated-person", "p")
                        .has("relocation-date", today())
        );

        LOG.query(city(), "insertRelocation", relocatePersonQuery);
        tx().execute(relocatePersonQuery);
    }
}