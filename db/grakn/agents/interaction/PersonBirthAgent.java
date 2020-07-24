package grakn.simulation.db.grakn.agents.interaction;

import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public class PersonBirthAgent extends grakn.simulation.db.common.agents.interaction.PersonBirthAgent {

    @Override
    protected void insertPerson(String email, String gender, String forename, String surname) {
        GraqlInsert query =
                Graql.match(
                        Graql.var("c").isa("city")
                                .has("location-name", city().toString()))
                        .insert(Graql.var("p").isa("person")
                                        .has("email", email)
                                        .has("date-of-birth", today())
                                        .has("gender", gender)
                                        .has("forename", forename)
                                        .has("surname", surname),
                                Graql.var("b").isa("born-in")
                                        .rel("born-in_child", "p")
                                        .rel("born-in_place-of-birth", "c")
                        );
        log().query("insertPerson", query);
        try (ThreadTrace trace = traceOnThread("execute")) {
            tx().forGrakn().execute(query);
        }
    }
}
