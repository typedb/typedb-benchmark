package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.grakn.driver.GraknClientWrapper;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.agents.interaction.RelocationAgent.cityResidentsQuery;

public class FriendshipAgent extends grakn.simulation.db.common.agents.interaction.FriendshipAgent<GraknClientWrapper.Session, GraknClientWrapper.Transaction> {

    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        return ExecutorUtils.getOrderedAttribute(tx().forGrakn(), cityResidentsQuery, "email");
    }

    protected void insertFriendship(String friend1Email, String friend2Email) {
        GraqlInsert insertFriendshipQuery = Graql.match(
                Graql.var("p1").isa("person").has("email", friend1Email),
                Graql.var("p2").isa("person").has("email", friend2Email),
                Graql.not(Graql.var()
                        .isa("friendship")
                        .rel("friendship_friend", Graql.var("p1"))
                        .rel("friendship_friend", Graql.var("p2")))
        ).insert(
                Graql.var("friendship")
                        .isa("friendship")
                        .rel("friendship_friend", Graql.var("p1"))
                        .rel("friendship_friend", Graql.var("p2"))
                        .has("start-date", today())
        );
        log().query("insertFriendship", insertFriendshipQuery);
        tx().forGrakn().execute(insertFriendshipQuery);
    }
}
