package grakn.simulation.db.grakn.agents;

import grakn.client.answer.Numeric;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.agents.RelocationAgent.cityResidentsQuery;

public class FriendshipAgent extends grakn.simulation.db.common.agents.interaction.FriendshipAgent {

    protected boolean checkIfFriendshipExists(String friend1Email, String friend2Email) {
        GraqlGet.Aggregate friendshipExistsQuery = Graql.match(
                Graql.var("p1").isa("person").has("email", friend1Email),
                Graql.var("p2").isa("person").has("email", friend2Email),
                Graql.var("friendship")
                        .rel("friendship_friend", Graql.var("p1"))
                        .rel("friendship_friend", Graql.var("p2"))).get().count();
        log().query("checkIfFriendshipExists", friendshipExistsQuery);
        List<Numeric> answer = tx().forGrakn().execute(friendshipExistsQuery).get();
        int numAnswers = answer.get(0).number().intValue();
        return numAnswers > 0;
    }

    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet.Unfiltered cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        return ExecutorUtils.getOrderedAttribute(tx().forGrakn(), cityResidentsQuery, "email");
    }

    protected void insertFriendship(String friend1Email, String friend2Email) {
        GraqlInsert insertFriendshipQuery = Graql.match(
                Graql.var("p1").isa("person").has("email", friend1Email),
                Graql.var("p2").isa("person").has("email", friend2Email)
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
