package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.FRIENDSHIP;
import static grakn.simulation.db.grakn.schema.Schema.FRIENDSHIP_FRIEND;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.START_DATE;

public class FriendshipAgent extends grakn.simulation.db.common.agents.interaction.FriendshipAgent {

    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        return ((Transaction)tx()).getOrderedAttribute(cityResidentsQuery, EMAIL, null);
    }

    protected void insertFriendship(String friend1Email, String friend2Email) {

        Statement person1 = Graql.var("p1");
        Statement person2 = Graql.var("p2");
        Statement friendship = Graql.var();

        GraqlInsert insertFriendshipQuery = Graql.match(
                person1
                        .isa(PERSON).has(EMAIL, friend1Email),
                person2
                        .isa(PERSON).has(EMAIL, friend2Email),
                Graql.not(
                        friendship
                                .isa(FRIENDSHIP)
                                .rel(FRIENDSHIP_FRIEND, person1)
                                .rel(FRIENDSHIP_FRIEND, person2)
                )
        ).insert(
                Graql.var(FRIENDSHIP)
                        .isa(FRIENDSHIP)
                        .rel(FRIENDSHIP_FRIEND, person1)
                        .rel(FRIENDSHIP_FRIEND, person2)
                        .has(START_DATE, today())
        );
        log().query("insertFriendship", insertFriendshipQuery);
        tx().forGrakn().execute(insertFriendshipQuery);
    }

    @Override
    protected int checkCount() {
        GraqlGet.Aggregate countQuery = Graql.match(

        ).get().count();
        return tx().forGrakn().execute(countQuery).get().get(0).number().intValue();
    }
}
