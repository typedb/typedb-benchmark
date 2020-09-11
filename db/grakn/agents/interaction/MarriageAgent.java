package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.MarriageAgentBase;
import grakn.simulation.db.grakn.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.StatementAttribute;

import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.grakn.agents.utils.Utils.getOnlyAttributeOfThing;
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

public class MarriageAgent extends MarriageAgentBase<GraknContext> {

    private Transaction tx;

    @Override
    protected void openTx() {
        if (tx == null) {
            tx = backendContext().tx(getSessionKey());
        }
    }

    @Override
    protected void closeTx() {
        tx.close();
        tx = null;
    }

    @Override
    protected void commitTx() {
        tx.commit();
        tx = null;
    }

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
            result = tx.getOrderedAttribute(query, EMAIL, null);
        }
        log().message(scope, result.toString());
        return result;
    }

    @Override
    protected AgentResult insertMarriage(int marriageIdentifier, String wifeEmail, String husbandEmail) {
        Statement husband = Graql.var("husband");
        Statement wife = Graql.var("wife");
        Statement city = Graql.var(CITY);
        Statement marriage = Graql.var("marriage");

        StatementAttribute cityNameVar = Graql.var().val(city().name());
        StatementAttribute marriageIdentifierVar = Graql.var().val(marriageIdentifier);
        StatementAttribute husbandEmailVar = Graql.var().val(husbandEmail);
        StatementAttribute wifeEmailVar = Graql.var().val(wifeEmail);

        GraqlInsert marriageQuery = Graql.match(
                husband.isa(PERSON).has(EMAIL, husbandEmailVar),
                wife.isa(PERSON).has(EMAIL, wifeEmailVar),
                city.isa(CITY).has(LOCATION_NAME, cityNameVar)
        ).insert(
                marriage.isa(MARRIAGE)
                        .rel(MARRIAGE_HUSBAND, husband)
                        .rel(MARRIAGE_WIFE, wife)
                        .has(MARRIAGE_ID, marriageIdentifierVar),
                Graql.var().isa(LOCATES).rel(LOCATES_LOCATED, marriage).rel(LOCATES_LOCATION, city)
        );

        List<ConceptMap> answers;
        log().query("insertMarriage", marriageQuery);
        try (ThreadTrace trace = traceOnThread("execute")) {
            answers = tx.execute(marriageQuery);
        }

        ConceptMap answer = getOnlyElement(answers);
        Object wifeEmailAns = tx.getOnlyAttributeOfThing(answer, "wife", EMAIL);
        Object husbandEmailAns = tx.getOnlyAttributeOfThing(answer, "husband", EMAIL);
        Object marriageIdentifierAns = tx.getOnlyAttributeOfThing(answer, "marriage", MARRIAGE_ID);
        Object cityNameAns = tx.getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME);

        return new AgentResult(){{
                put(MarriageAgentField.MARRIAGE_IDENTIFIER, marriageIdentifierAns);
                put(MarriageAgentField.WIFE_EMAIL, wifeEmailAns);
                put(MarriageAgentField.HUSBAND_EMAIL, husbandEmailAns);
                put(MarriageAgentField.CITY_NAME, cityNameAns);
            }};
    }

    @Override
    protected int checkCount() {
        Statement husband = Graql.var("husband");
        Statement wife = Graql.var("wife");
        Statement city = Graql.var(CITY);
        Statement marriage = Graql.var(MARRIAGE);

        Statement cityNameVar = Graql.var().val(city().name());
        Statement marriageIdentifierVar = Graql.var(MARRIAGE_ID);
        Statement wifeEmailVar = Graql.var("wife-email");
        Statement husbandEmailVar = Graql.var("husband-email");

        GraqlGet.Aggregate countQuery = Graql.match(
                husband.isa(PERSON).has(EMAIL, husbandEmailVar),
                wife.isa(PERSON).has(EMAIL, wifeEmailVar),
                city.isa(CITY).has(LOCATION_NAME, cityNameVar),
                marriage.isa(MARRIAGE)
                        .rel(MARRIAGE_WIFE, wife)
                        .rel(MARRIAGE_HUSBAND, husband)
                        .has(MARRIAGE_ID, marriageIdentifierVar),
                Graql.var().isa(LOCATES).rel(LOCATES_LOCATED, marriage).rel(LOCATES_LOCATION, city)
        ).get().count();
        log().query("checkCount", countQuery);
        return tx.count(countQuery);
    }
}
