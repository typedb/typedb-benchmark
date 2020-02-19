package grakn.simulation.agents;

import grakn.simulation.agents.common.CityAgent;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MarriageAgent extends CityAgent {

    private static final World.WorldLogWrapper LOG = World.log(MarriageAgent.class);
    private static final int NUM_MARRIAGES = 5;

    @Override
    public void iterate() {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        List<String> womenEmails = getSingleWomen();
        shuffle(womenEmails);

        List<String> menEmails = getSingleMen();
        shuffle(menEmails);

        int numMarriagesPossible = Math.min(NUM_MARRIAGES, Math.min(womenEmails.size(), menEmails.size()));

        if (numMarriagesPossible > 0) {

            for (int i = 0; i < numMarriagesPossible; i++) {
                insertMarriage(womenEmails.get(i), menEmails.get(i));
            }
            tx().commit();
        }
    }

    private LocalDateTime dobOfAdults() {
        return today().minusYears(World.AGE_OF_ADULTHOOD);
    }

    private List<String> getSingleWomen() {
        GraqlGet.Unfiltered singleWomenQuery = getSinglePeopleOfGenderQuery("female", "marriage_wife");
        LOG.query(city(), "getSingleWomen", singleWomenQuery);
        return tx().execute(singleWomenQuery).stream().map(a -> a.get("email").asAttribute().value().toString()).sorted().collect(Collectors.toList());
    }

    private List<String> getSingleMen() {
        GraqlGet.Unfiltered singleMenQuery = getSinglePeopleOfGenderQuery("male", "marriage_husband");
        LOG.query(city(), "getSingleMen", singleMenQuery);
        return tx().execute(singleMenQuery).stream().map(a -> a.get("email").asAttribute().value().toString()).sorted().collect(Collectors.toList());
    }

    private GraqlGet.Unfiltered getSinglePeopleOfGenderQuery(String gender, String marriageRole) {
        Statement personVar = Graql.var("p");
        Statement cityVar = Graql.var("city");

        return Graql.match(
                personVar.isa("person").has("gender", gender).has("email", Graql.var("email")).has("date-of-birth", Graql.var("dob")),
                Graql.var("dob").lte(dobOfAdults()),
                Graql.not(Graql.var("m").isa("marriage").rel(marriageRole, personVar)),
                Graql.var("r").isa("residency").rel("residency_resident", personVar).rel("residency_location", cityVar),
                Graql.not(Graql.var("r").has("end-date", Graql.var("ed"))),
                cityVar.isa("city").has("name", city().getName())
        ).get("email");
    }

    private void insertMarriage(String wifeEmail, String husbandEmail) {
        int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();

        GraqlInsert marriageQuery = Graql.match(
                Graql.var("husband").isa("person").has("email", husbandEmail),
                Graql.var("wife").isa("person").has("email", wifeEmail),
                Graql.var("city").isa("city").has("name", city().getName())
        ).insert(
                Graql.var("m").isa("marriage")
                        .rel("marriage_husband", "husband")
                        .rel("marriage_wife", "wife")
                        .has("marriage-id", marriageIdentifier),
                Graql.var().isa("locates").rel("locates_located", Graql.var("m")).rel("locates_location", Graql.var("city"))
        );
        LOG.query(city(), "insertMarriage", marriageQuery);
        tx().execute(marriageQuery);
    }
}
