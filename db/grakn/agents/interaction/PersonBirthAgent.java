package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
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

public class PersonBirthAgent extends grakn.simulation.db.common.agents.interaction.PersonBirthAgent {

    @Override
    protected void insertPerson(String email, String gender, String forename, String surname) {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var("p");
        Statement bornIn = Graql.var("b");
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
        log().query("insertPerson", query);
        try (ThreadTrace trace = traceOnThread("execute")) {
            tx().forGrakn().execute(query).get();
        }
    }

    @Override
    protected int checkCount() {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var("p");
        Statement bornIn = Graql.var("b");

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
