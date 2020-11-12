package grakn.simulation.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.SpouseType;
import grakn.simulation.common.action.write.InsertParentShipAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.util.HashMap;

import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.PARENTSHIP;
import static grakn.simulation.grakn.action.Model.PARENTSHIP_CHILD;
import static grakn.simulation.grakn.action.Model.PARENTSHIP_PARENT;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknInsertParentShipAction extends InsertParentShipAction<GraknOperation, ConceptMap> {
    public GraknInsertParentShipAction(GraknOperation dbOperation, HashMap<SpouseType, String> marriage, String childEmail) {
        super(dbOperation, marriage, childEmail);
    }

    @Override
    public ConceptMap run() {
        // Parentship where parents have multiple children is represented as multiple ternary relations, each with
        // both parents and one child. They had these children at the same time, and will not have any subsequently.
        GraqlInsert parentshipQuery = query(marriage, childEmail);
        return Action.singleResult(dbOperation.execute(parentshipQuery));

    }

    public static GraqlInsert query(HashMap<SpouseType, String> marriage, String childEmail) {
        Statement parentship = Graql.var(PARENTSHIP);
        Statement child = Graql.var("child");
        Statement mother = Graql.var("mother");
        Statement father = Graql.var("father");

        return Graql.match(
                mother.isa(PERSON).has(EMAIL, marriage.get(SpouseType.WIFE)),
                father.isa(PERSON).has(EMAIL, marriage.get(SpouseType.HUSBAND)),
                child.isa(PERSON).has(EMAIL, childEmail)
        ).insert(
                parentship.isa(PARENTSHIP)
                        .rel(PARENTSHIP_PARENT, father)
                        .rel(PARENTSHIP_PARENT, mother)
                        .rel(PARENTSHIP_CHILD, child)
        );
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object> () {
            {
                put(InsertParentShipActionField.WIFE_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "mother", EMAIL));
                put(InsertParentShipActionField.HUSBAND_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "father", EMAIL));
                put(InsertParentShipActionField.CHILD_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "child", EMAIL));
            }
        };
    }

    public enum InsertParentShipActionField implements ComparableField {
        WIFE_EMAIL, HUSBAND_EMAIL, CHILD_EMAIL
    }
}
