/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.grakn.action.write;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.SpouseType;
import grakn.benchmark.simulation.action.write.InsertParentShipAction;
import grakn.client.api.answer.ConceptMap;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlInsert;

import java.util.HashMap;

import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.PARENTSHIP;
import static grakn.benchmark.grakn.action.Model.PARENTSHIP_CHILD;
import static grakn.benchmark.grakn.action.Model.PARENTSHIP_PARENT;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknInsertParentShipAction extends InsertParentShipAction<GraknTransaction, ConceptMap> {

    public GraknInsertParentShipAction(GraknTransaction tx, HashMap<SpouseType, String> marriage, String childEmail) {
        super(tx, marriage, childEmail);
    }

    @Override
    public ConceptMap run() {
        // Parentship where parents have multiple children is represented as multiple ternary relations, each with
        // both parents and one child. They had these children at the same time, and will not have any subsequently.
        GraqlInsert parentshipQuery = query(marriage, childEmail);
        return Action.singleResult(tx.execute(parentshipQuery));

    }

    public static GraqlInsert query(HashMap<SpouseType, String> marriage, String childEmail) {
        UnboundVariable parentship = var(PARENTSHIP);
        UnboundVariable child = var("child");
        UnboundVariable mother = var("mother");
        UnboundVariable father = var("father");

        return match(
                mother.isa(PERSON).has(EMAIL, marriage.get(SpouseType.WIFE)),
                father.isa(PERSON).has(EMAIL, marriage.get(SpouseType.HUSBAND)),
                child.isa(PERSON).has(EMAIL, childEmail)
        ).insert(
                parentship
                        .rel(PARENTSHIP_PARENT, father)
                        .rel(PARENTSHIP_PARENT, mother)
                        .rel(PARENTSHIP_CHILD, child)
                        .isa(PARENTSHIP)
        );
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertParentShipActionField.WIFE_EMAIL, tx.getOnlyAttributeOfThing(answer, "mother", EMAIL));
                put(InsertParentShipActionField.HUSBAND_EMAIL, tx.getOnlyAttributeOfThing(answer, "father", EMAIL));
                put(InsertParentShipActionField.CHILD_EMAIL, tx.getOnlyAttributeOfThing(answer, "child", EMAIL));
            }
        };
    }

    public enum InsertParentShipActionField implements ComparableField {
        WIFE_EMAIL, HUSBAND_EMAIL, CHILD_EMAIL
    }
}
