package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.simulation.common.Allocation;
import grakn.simulation.common.ExecutorUtils;
import grakn.simulation.common.LogWrapper;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static grakn.simulation.common.ExecutorUtils.getOrderedAttribute;
import static java.util.stream.Collectors.toList;

public class RelocationAgent implements CityAgent {

    private static final LogWrapper<World.City> LOG = new LogWrapper<>(LoggerFactory.getLogger(PersonBirthAgent.class), World.City::getTracker);
    private static final int NUM_RELOCATIONS = 5;

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */
        Random random = randomSource.startNewRandom();
        GraknClient.Session session = context.getIterationGraknSessionFor(city.getCountry().getContinent().getName());

        LocalDateTime earliestDate;
        if (context.getLocalDateTime().minusYears(2).isBefore(LocalDateTime.of(LocalDate.ofYearDay(0, 1), LocalTime.of(0, 0, 0))))
            earliestDate = context.getLocalDateTime();
        else {
            earliestDate = context.getLocalDateTime().minusYears(2);
        }

        LocalDateTime dateToday = context.getLocalDateTime();

        try (GraknClient.Transaction tx = session.transaction().write()) {

            List<String> residentEmails = getResidentEmails(tx, city, earliestDate);
            Collections.shuffle(residentEmails, random);

            List<String> relocationCityNames = getRelocationCityNames(tx, city);

            Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) ->insertRelocation(tx, dateToday, city, residentEmail, relocationCityName));

            tx.commit();
        }
    }

    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate) {
        return Graql.match(
                Graql.var("person").isa("person").has("email", Graql.var("email")),
                Graql.var("city").isa("city").has("name", city.getName()),
                Graql.var("r").isa("residency")
                        .rel("residency_resident", "person")
                        .rel("residency_location", "city")
                        .has("start-date", Graql.var("start-date")),
                Graql.not(Graql.var("r").has("end-date", Graql.var("ed"))),
                Graql.var("start-date").lte(earliestDate)
        ).get();
    }

    private List<String> getResidentEmails(GraknClient.Transaction tx, World.City city, LocalDateTime earliestDate) {
        GraqlGet.Unfiltered cityResidentsQuery = cityResidentsQuery(city, earliestDate);
        LOG.query(city, "getResidentEmails", cityResidentsQuery);
        return ExecutorUtils.getOrderedAttribute(tx, cityResidentsQuery, "email", NUM_RELOCATIONS);
    }

    private List<String> getRelocationCityNames(GraknClient.Transaction tx, World.City city) {

        GraqlGet.Unfiltered relocationCitiesQuery = Graql.match(
                Graql.var("city").isa("city").has("name", Graql.var("city-name")),
                Graql.var("continent").isa("continent").has("name", city.getCountry().getContinent().getName()),
                Graql.var("lh1").isa("location-hierarchy").rel("city").rel("continent"),
                Graql.var("city-name").neq(city.getName())
        ).get();

        LOG.query(city, "getRelocationCityNames", relocationCitiesQuery);
//        return getOrderedAttribute(tx, relocationCitiesQuery, "city-name");
        return tx.execute(relocationCitiesQuery)
                                .stream()
                                .map(conceptMap -> conceptMap.get("city-name").asAttribute().value().toString())
                                .collect(toList());
    }

    private void insertRelocation(GraknClient.Transaction tx, LocalDateTime dateToday, World.City city, String email, String newCityName) {
        GraqlInsert relocatePersonQuery = Graql.match(
                Graql.var("p").isa("person").has("email", email),
                Graql.var("new-city").isa("city").has("name", newCityName),
                Graql.var("old-city").isa("city").has("name", city.getName())
        ).insert(
                Graql.var("r").isa("relocation")
                        .rel("relocation_previous-location", "old-city")
                        .rel("relocation_new-location", "new-city")
                        .rel("relocation_relocated-person", "p")
                        .has("relocation-date", dateToday)
        );

        LOG.query(city, "insertRelocation", relocatePersonQuery);
        tx.execute(relocatePersonQuery);
    }
}