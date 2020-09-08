package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.interaction.PersonBirthAgentBase;
import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.util.HashMap;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.grakn.agents.utils.Utils.getOnlyAttributeOfThing;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.FORENAME;
import static grakn.simulation.db.grakn.schema.Schema.GENDER;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.SURNAME;

public class PersonBirthAgent extends PersonBirthAgentBase {

    @Override
    protected HashMap<Field, Object> insertPerson(String email, String gender, String forename, String surname) {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var(PERSON);
        Statement bornIn = Graql.var(BORN_IN);
        Statement emailVar = Graql.var(EMAIL);
        Statement genderVar = Graql.var(GENDER);
        Statement forenameVar = Graql.var(FORENAME);
        Statement surnameVar = Graql.var(SURNAME);
        Statement dobVar = Graql.var(DATE_OF_BIRTH);

        GraqlInsert query =
                Graql.match(
                        city.isa(CITY)
                                .has(LOCATION_NAME, city().name()))
                        .insert(
                                person.isa(PERSON)
                                        .has(EMAIL, emailVar)
                                        .has(DATE_OF_BIRTH, dobVar)
                                        .has(GENDER, genderVar)
                                        .has(FORENAME, forenameVar)
                                        .has(SURNAME, surnameVar),
                                bornIn
                                        .isa(BORN_IN)
                                        .rel(BORN_IN_CHILD, person)
                                        .rel(BORN_IN_PLACE_OF_BIRTH, city),
                                emailVar.val(email),
                                genderVar.val(gender),
                                forenameVar.val(forename),
                                surnameVar.val(surname),
                                dobVar.val(today())
                        );

        List<ConceptMap> answers;
        log().query("insertPerson", query);
        try (ThreadTrace trace = traceOnThread("execute")) {
            answers = tx().forGrakn().execute(query).get();
        }

        ConceptMap answer = getOnlyElement(answers);
        return new HashMap<Field, Object>(){
            {
                put(PersonBirthAgentField.EMAIL, getOnlyAttributeOfThing(answer, tx().forGrakn(), PERSON, EMAIL));
                put(PersonBirthAgentField.DATE_OF_BIRTH, getOnlyAttributeOfThing(answer, tx().forGrakn(), PERSON, DATE_OF_BIRTH));
                put(PersonBirthAgentField.GENDER, getOnlyAttributeOfThing(answer, tx().forGrakn(), PERSON, GENDER));
                put(PersonBirthAgentField.FORENAME, getOnlyAttributeOfThing(answer, tx().forGrakn(), PERSON, FORENAME));
                put(PersonBirthAgentField.SURNAME, getOnlyAttributeOfThing(answer, tx().forGrakn(), PERSON, SURNAME));
            }};
    }

    @Override
    protected int checkCount() {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var(PERSON);
        Statement bornIn = Graql.var(BORN_IN);

        Statement emailVar = Graql.var(EMAIL);
        Statement genderVar = Graql.var(GENDER);
        Statement forenameVar = Graql.var(FORENAME);
        Statement surnameVar = Graql.var(SURNAME);
        Statement dobVar = Graql.var(DATE_OF_BIRTH);

        GraqlGet.Aggregate countQuery = Graql.match(
                city.isa(CITY)
                        .has(LOCATION_NAME, city().name()),
                person.isa(PERSON)
                        .has(EMAIL, emailVar)
                        .has(DATE_OF_BIRTH, dobVar)
                        .has(GENDER, genderVar)
                        .has(FORENAME, forenameVar)
                        .has(SURNAME, surnameVar),
                bornIn
                        .isa(BORN_IN)
                        .rel(BORN_IN_CHILD, person)
                        .rel(BORN_IN_PLACE_OF_BIRTH, city)
        ).get().count();
        return ((Transaction) tx()).count(countQuery);
    }
}
