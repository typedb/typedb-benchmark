/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.simulation.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.write.InsertFriendshipAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.StatementAttribute;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.FRIENDSHIP;
import static grakn.simulation.grakn.action.Model.FRIENDSHIP_FRIEND;
import static grakn.simulation.grakn.action.Model.PERSON;
import static grakn.simulation.grakn.action.Model.START_DATE;

public class GraknInsertFriendshipAction extends InsertFriendshipAction<GraknOperation, ConceptMap> {

    public GraknInsertFriendshipAction(GraknOperation dbOperation, LocalDateTime today, String friend1Email, String friend2Email) {
        super(dbOperation, today, friend1Email, friend2Email);
    }

    @Override
    public ConceptMap run() {
        return Action.optionalSingleResult(dbOperation.execute(query(today, friend1Email, friend2Email)));
    }

    public static GraqlInsert query(LocalDateTime today, String friend1Email, String friend2Email) {
        Statement person1 = Graql.var("p1");
        Statement person2 = Graql.var("p2");
        Statement friendship = Graql.var();

        StatementAttribute friend1EmailVar = Graql.var().val(friend1Email);
        StatementAttribute friend2EmailVar = Graql.var().val(friend2Email);
        StatementAttribute startDate = Graql.var().val(today);

        return Graql.match(
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
