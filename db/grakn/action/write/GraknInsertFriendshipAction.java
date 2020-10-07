package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.write.InsertFriendshipAction;
import grakn.simulation.db.common.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.StatementAttribute;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.FRIENDSHIP;
import static grakn.simulation.db.grakn.schema.Schema.FRIENDSHIP_FRIEND;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.START_DATE;

public class GraknInsertFriendshipAction extends InsertFriendshipAction<GraknOperation, ConceptMap> {

    public GraknInsertFriendshipAction(GraknOperation dbOperation, LocalDateTime today, String friend1Email, String friend2Email) {
        super(dbOperation, today, friend1Email, friend2Email);
    }

    @Override
    public ConceptMap run() {
        Statement person1 = Graql.var("p1");
        Statement person2 = Graql.var("p2");
        Statement friendship = Graql.var();

        StatementAttribute friend1EmailVar = Graql.var().val(friend1Email);
        StatementAttribute friend2EmailVar = Graql.var().val(friend2Email);
        StatementAttribute startDate = Graql.var().val(today);

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
        return Action.optionalSingleResult(dbOperation.execute(insertFriendshipQuery));
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertFriendshipActionField.FRIEND1_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "p1", EMAIL));
                put(InsertFriendshipActionField.FRIEND2_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "p2", EMAIL));
                put(InsertFriendshipActionField.START_DATE, dbOperation.getOnlyAttributeOfThing(answer, FRIENDSHIP, START_DATE));
            }
        };

    }
}
