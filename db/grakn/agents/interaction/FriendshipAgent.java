package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.grakn.context.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.StatementAttribute;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.FRIENDSHIP;
import static grakn.simulation.db.grakn.schema.Schema.FRIENDSHIP_FRIEND;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.START_DATE;

public class FriendshipAgent extends grakn.simulation.db.common.agents.interaction.FriendshipAgent<GraknContext> {

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

    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        return tx.getOrderedAttribute(cityResidentsQuery, EMAIL, null);
    }

    protected void insertFriendship(String friend1Email, String friend2Email) {

        Statement person1 = Graql.var("p1");
        Statement person2 = Graql.var("p2");
        Statement friendship = Graql.var();

        StatementAttribute friend1EmailVar = Graql.var().val(friend1Email);
        StatementAttribute friend2EmailVar = Graql.var().val(friend2Email);
        StatementAttribute startDate = Graql.var().val(today());

        GraqlInsert insertFriendshipQuery = Graql.match(
                person1
                        .isa(PERSON).has(EMAIL, friend1EmailVar),
                person2
                        .isa(PERSON).has(EMAIL, friend2EmailVar),
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
                        .has(START_DATE, startDate)
        );
        log().query("insertFriendship", insertFriendshipQuery);
        tx.execute(insertFriendshipQuery);
    }

    @Override
    protected int checkCount() {
        Statement person1 = Graql.var("p1");
        Statement person2 = Graql.var("p2");
        Statement friendship = Graql.var();

        Statement friend1EmailVar = Graql.var("friend1-email");
        Statement friend2EmailVar = Graql.var("friend2-email");
        Statement startDate = Graql.var(START_DATE);

        GraqlGet.Aggregate countQuery = Graql.match(
                person1
                        .isa(PERSON).has(EMAIL, friend1EmailVar),
                person2
                        .isa(PERSON).has(EMAIL, friend2EmailVar),
                Graql.not(
                        friendship
                                .isa(FRIENDSHIP)
                                .rel(FRIENDSHIP_FRIEND, person1)
                                .rel(FRIENDSHIP_FRIEND, person2)
                ),
                Graql.var(FRIENDSHIP)
                        .isa(FRIENDSHIP)
                        .rel(FRIENDSHIP_FRIEND, person1)
                        .rel(FRIENDSHIP_FRIEND, person2)
                        .has(START_DATE, startDate)
        ).get().count();
        log().query("checkCount", countQuery);
        return tx.count(countQuery);
    }
}
