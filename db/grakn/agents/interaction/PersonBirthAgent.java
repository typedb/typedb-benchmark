package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

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
        GraqlInsert query =
                Graql.match(
                        Graql.var("c").isa(CITY)
                                .has(LOCATION_NAME, city().toString()))
                        .insert(Graql.var("p").isa(PERSON)
                                        .has(EMAIL, email)
                                        .has(DATE_OF_BIRTH, today())
                                        .has(GENDER, gender)
                                        .has(FORENAME, forename)
                                        .has(SURNAME, surname),
                                Graql.var("b").isa(BORN_IN)
                                        .rel(BORN_IN_CHILD, "p")
                                        .rel(BORN_IN_PLACE_OF_BIRTH, "c")
                        );
        log().query("insertPerson", query);
        try (ThreadTrace trace = traceOnThread("execute")) {
            tx().forGrakn().execute(query);
        }
    }

    @Override
    protected int checkCount() {
//        GraqlGet.Aggregate countQuery = Graql.match(
//
//        ).get().count();
//        return ((Transaction) tx()).count(countQuery);
        return 0;
    }
}
