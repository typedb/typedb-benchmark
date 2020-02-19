package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.client.GraknClient.Transaction;
import grakn.simulation.common.LogWrapper;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static grakn.simulation.common.ExecutorUtils.getOrderedAttribute;

public class MarriageAgent implements CityAgent {

    private static final LogWrapper<World.City> LOG = new LogWrapper<>(LoggerFactory.getLogger(PersonBirthAgent.class), World.City::getTracker);
    private static final int NUM_MARRIAGES = 5;

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {
        String sessionKey = city.getCountry().getContinent().getName();
        GraknClient.Session session = context.getIterationGraknSessionFor(sessionKey);

        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        try ( Transaction tx = session.transaction().write()) {

            Random rnd = randomSource.startNewRandom();

            LocalDateTime dobOfAdults = context.getLocalDateTime().minusYears(World.AGE_OF_ADULTHOOD);

            List<String> womenEmails = getSingleWomen(tx, dobOfAdults, city);
            Collections.shuffle(womenEmails, rnd);

            List<String> menEmails = getSingleMen(tx, dobOfAdults, city);
            Collections.shuffle(menEmails, rnd);

            int numMarriagesPossible = Math.min(NUM_MARRIAGES, Math.min(womenEmails.size(), menEmails.size()));

            if (numMarriagesPossible > 0) {

                for (int i = 0; i < numMarriagesPossible; i++) {
                    insertMarriage(tx, city, womenEmails.get(i), menEmails.get(i));
                }
                tx.commit();
            }
        }
    }

    private List<String> getSingleWomen(Transaction tx, LocalDateTime dobOfAdults, World.City city) {
        GraqlGet.Unfiltered singleWomenQuery = getSinglePeopleOfGenderQuery(dobOfAdults, city, "female", "marriage_wife");
        LOG.query(city, "getSingleWomen", singleWomenQuery);
        return getOrderedAttribute(tx, singleWomenQuery, "email");
    }

    private List<String> getSingleMen(Transaction tx, LocalDateTime dobOfAdults, World.City city) {
        GraqlGet.Unfiltered singleMenQuery = getSinglePeopleOfGenderQuery(dobOfAdults, city, "male", "marriage_husband");
        LOG.query(city, "getSingleMen", singleMenQuery);
        return getOrderedAttribute(tx, singleMenQuery, "email");
    }

    private GraqlGet.Unfiltered getSinglePeopleOfGenderQuery(LocalDateTime dobOfAdults, World.City city, String gender, String marriageRole) {
        Statement personVar = Graql.var("p");
        Statement cityVar = Graql.var("city");

        return Graql.match(
                personVar.isa("person").has("gender", gender).has("email", Graql.var("email")).has("date-of-birth", Graql.var("dob")),
                Graql.var("dob").lte(dobOfAdults),
                Graql.not(Graql.var("m").isa("marriage").rel(marriageRole, personVar)),
                Graql.var("r").isa("residency").rel("residency_resident", personVar).rel("residency_location", cityVar),
                Graql.not(Graql.var("r").has("end-date", Graql.var("ed"))),
                cityVar.isa("city").has("name", city.getName())
        ).get("email");
    }

    private void insertMarriage(Transaction tx, World.City city, String wifeEmail, String husbandEmail) {
        int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();

        GraqlInsert marriageQuery = Graql.match(
                Graql.var("husband").isa("person").has("email", husbandEmail),
                Graql.var("wife").isa("person").has("email", wifeEmail),
                Graql.var("city").isa("city").has("name", city.getName())
        ).insert(
                Graql.var("m").isa("marriage")
                        .rel("marriage_husband", "husband")
                        .rel("marriage_wife", "wife")
                        .has("marriage-id", marriageIdentifier),
                Graql.var().isa("locates").rel("locates_located", Graql.var("m")).rel("locates_location", Graql.var("city"))
        );
        LOG.query(city, "insertMarriage", marriageQuery);
        tx.execute(marriageQuery);
    }
}
