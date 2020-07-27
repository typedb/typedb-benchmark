package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public class MarriageAgent extends grakn.simulation.db.common.agents.interaction.MarriageAgent {

    @Override
    protected List<String> getSingleWomen() {
        return getSinglePeopleOfGenderQuery("getSingleWomen", "female", "marriage_wife");
    }

    @Override
    protected List<String> getSingleMen() {
        return getSinglePeopleOfGenderQuery("getSingleMen", "male", "marriage_husband");
    }

    private List<String> getSinglePeopleOfGenderQuery(String scope, String gender, String marriageRole) {
        Statement personVar = Graql.var("p");
        Statement cityVar = Graql.var("city");

        GraqlGet query = Graql.match(
                personVar.isa("person").has("gender", gender).has("email", Graql.var("email")).has("date-of-birth", Graql.var("dob")),
                Graql.var("dob").lte(dobOfAdults()),
                Graql.not(Graql.var("m").isa("marriage").rel(marriageRole, personVar)),
                Graql.var("r").isa("residency").rel("residency_resident", personVar).rel("residency_location", cityVar),
                Graql.not(Graql.var("r").has("end-date", Graql.var("ed"))),
                cityVar.isa("city").has("location-name", city().name())
        ).get("email");

        log().query(scope, query);
        List<String> result;
        try (ThreadTrace trace = traceOnThread("execute")) {
            result = ((Transaction)tx()).getOrderedAttribute(query, "email", null);
        }
        return result;
    }

    @Override
    protected void insertMarriage(int marriageIdentifier, String wifeEmail, String husbandEmail) {

        GraqlInsert marriageQuery = Graql.match(
                Graql.var("husband").isa("person").has("email", husbandEmail),
                Graql.var("wife").isa("person").has("email", wifeEmail),
                Graql.var("city").isa("city").has("location-name", city().name())
        ).insert(
                Graql.var("m").isa("marriage")
                        .rel("marriage_husband", "husband")
                        .rel("marriage_wife", "wife")
                        .has("marriage-id", marriageIdentifier),
                Graql.var().isa("locates").rel("locates_located", Graql.var("m")).rel("locates_location", Graql.var("city"))
        );
        log().query("insertMarriage", marriageQuery);
        try (ThreadTrace trace = traceOnThread("execute")) {
            tx().forGrakn().execute(marriageQuery);
        }
    }
}
