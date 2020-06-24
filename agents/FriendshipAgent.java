package grakn.simulation.agents;

import grakn.client.answer.Numeric;
import grakn.simulation.agents.common.CityAgent;
import grakn.simulation.common.ExecutorUtils;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.agents.RelocationAgent.cityResidentsQuery;

public class FriendshipAgent extends CityAgent {

    @Override
    public void iterate() {

        List<String> residentEmails = getResidentEmails(today());
        closeTx();  // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585
        if (residentEmails.size() > 0) {
            shuffle(residentEmails);
            int numFriendships = world().getScaleFactor();
            for (int i = 0; i < numFriendships; i++) {

                String friend1 = pickOne(residentEmails);
                String friend2 = pickOne(residentEmails);

                if (!checkIfFriendshipExists(friend1, friend2)) {
                    insertFriendship(friend1, friend2);
                }
            }
            tx().commit();
        }
    }

    private boolean checkIfFriendshipExists(String friend1Email, String friend2Email) {
        GraqlGet.Aggregate friendshipExistsQuery = Graql.match(
                Graql.var("p1").isa("person").has("email", friend1Email),
                Graql.var("p2").isa("person").has("email", friend2Email),
                Graql.var("friendship")
                        .rel("friendship_friend", Graql.var("p1"))
                        .rel("friendship_friend", Graql.var("p2"))).get().count();
        log().query("checkIfFriendshipExists", friendshipExistsQuery);
        List<Numeric> answer = tx().execute(friendshipExistsQuery).get();
        int numAnswers = answer.get(0).number().intValue();
        return numAnswers > 0;
    }

    private List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet.Unfiltered cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        return ExecutorUtils.getOrderedAttribute(tx(), cityResidentsQuery, "email");
    }

    private void insertFriendship(String friend1Email, String friend2Email) {
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
        tx().execute(insertFriendshipQuery);
    }
}
