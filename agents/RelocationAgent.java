package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class RelocationAgent implements CityAgent {
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
        if (context.getLocalDateTime().minusYears(2).isBefore(LocalDateTime.of(LocalDate.ofYearDay(0, 1), LocalTime.of(0, 0, 0)))) {
            earliestDate = context.getLocalDateTime();
        } else {
            earliestDate = context.getLocalDateTime().minusYears(2);
        }

        List<String> residentEmails;
        List<String> relocationCityNames;

        try (GraknClient.Transaction tx = session.transaction().write()) {

            GraqlGet.Unfiltered cityResidentsQuery = Graql.match(
                    Graql.var("person").isa("person").has("email", Graql.var("email")),
                    Graql.var("city").isa("city").has("name", city.getName()),
                    Graql.var("r").isa("residency")
                            .rel("residency_resident", "person")
                            .rel("residency_location", "city")
                            .has("start-date", Graql.var("start-date")),
                    Graql.not(Graql.var("r").has("end-date")),
                    Graql.var("start-date").lte(earliestDate)
            ).get();
            System.out.println(cityResidentsQuery);
            List<ConceptMap> residentsAnswers = tx.execute(cityResidentsQuery);

            Collections.shuffle(residentsAnswers, random);

            residentEmails = residentsAnswers.stream()
                    .limit(NUM_RELOCATIONS)
                    .map(conceptMap -> conceptMap.get("email").asAttribute().value().toString())
                    .collect(toList());

            GraqlGet.Unfiltered relocationCitiesQuery = Graql.match(
                    Graql.var("city").isa("city").has("name", Graql.var("city-name")),
                    Graql.var("continent").isa("continent").has("name", city.getCountry().getContinent().getName()),
                    Graql.var("lh1").isa("location-hierarchy").rel("city").rel("continent"),
                    Graql.var("city-name").neq(city.getName())
            ).get();

            System.out.println(relocationCitiesQuery);
            relocationCityNames = tx.execute(relocationCitiesQuery)
                    .stream()
                    .map(conceptMap -> conceptMap.get("city-name").asAttribute().value().toString())
                    .collect(toList());
            tx.commit();
        }
        try (GraknClient.Transaction tx = session.transaction().write()) {

            if (relocationCityNames.size() > 0 && residentEmails.size() > 0) {
                List<Integer> cityAllocationPerPerson = new ArrayList<>();
                for (int i = 0; i < residentEmails.size(); i++) {
                    cityAllocationPerPerson.add(i % relocationCityNames.size());
                }

                for (int i = 0; i < cityAllocationPerPerson.size(); i++) {

                    String email = residentEmails.get(i);
                    String newCityName = relocationCityNames.get(cityAllocationPerPerson.get(i));

                    GraqlInsert relocatePersonQuery = Graql.match(
                            Graql.var("p").isa("person").has("email", email),
                            Graql.var("new-city").isa("city").has("name", newCityName),
                            Graql.var("old-city").isa("city").has("name", city.getName())
                    ).insert(
                            Graql.var("r").isa("relocation")
                                    .rel("relocation_previous-location", "old-city")
                                    .rel("relocation_new-location", "new-city")
                                    .rel("relocation_relocated-person", "p")
                                    .has("relocation-date", context.getLocalDateTime())
                    );
                    System.out.println(relocatePersonQuery);
                    tx.execute(relocatePersonQuery);
                }
                tx. commit();
            }
        }
    }
}