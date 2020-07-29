package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.END_DATE;
import static grakn.simulation.db.grakn.schema.Schema.GENDER;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATED;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_HUSBAND;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_ID;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_WIFE;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_RESIDENT;

public class MarriageAgent extends grakn.simulation.db.common.agents.interaction.MarriageAgent {

    @Override
    protected List<String> getSingleWomen() {
        return getSinglePeopleOfGenderQuery("getSingleWomen", "female", MARRIAGE_WIFE);
    }

    @Override
    protected List<String> getSingleMen() {
        return getSinglePeopleOfGenderQuery("getSingleMen", "male", MARRIAGE_HUSBAND);
    }

    private List<String> getSinglePeopleOfGenderQuery(String scope, String gender, String marriageRole) {
        Statement personVar = Graql.var(PERSON);
        Statement cityVar = Graql.var(CITY);

        GraqlGet query = Graql.match(
                personVar.isa(PERSON).has(GENDER, gender).has(EMAIL, Graql.var(EMAIL)).has(DATE_OF_BIRTH, Graql.var(DATE_OF_BIRTH)),
                Graql.var(DATE_OF_BIRTH).lte(dobOfAdults()),
                Graql.not(Graql.var("m").isa(MARRIAGE).rel(marriageRole, personVar)),
                Graql.var("r").isa(RESIDENCY).rel(RESIDENCY_RESIDENT, personVar).rel(RESIDENCY_LOCATION, cityVar),
                Graql.not(Graql.var("r").has(END_DATE, Graql.var(END_DATE))),
                cityVar.isa(CITY).has(LOCATION_NAME, city().name())
        ).get(EMAIL);

        log().query(scope, query);
        List<String> result;
        try (ThreadTrace trace = traceOnThread("execute")) {
            result = ((Transaction)tx()).getOrderedAttribute(query, EMAIL, null);
        }
        return result;
    }

    @Override
    protected void insertMarriage(int marriageIdentifier, String wifeEmail, String husbandEmail) {

        GraqlInsert marriageQuery = Graql.match(
                Graql.var("husband").isa(PERSON).has(EMAIL, husbandEmail),
                Graql.var("wife").isa(PERSON).has(EMAIL, wifeEmail),
                Graql.var(CITY).isa(CITY).has(LOCATION_NAME, city().name())
        ).insert(
                Graql.var("m").isa(MARRIAGE)
                        .rel(MARRIAGE_HUSBAND, "husband")
                        .rel(MARRIAGE_WIFE, "wife")
                        .has(MARRIAGE_ID, marriageIdentifier),
                Graql.var().isa(LOCATES).rel(LOCATES_LOCATED, Graql.var("m")).rel(LOCATES_LOCATION, Graql.var(CITY))
        );
        log().query("insertMarriage", marriageQuery);
        try (ThreadTrace trace = traceOnThread("execute")) {
            tx().forGrakn().execute(marriageQuery);
        }
    }
}
