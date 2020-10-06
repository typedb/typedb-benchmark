package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.write.InsertParentShipAction;
import grakn.simulation.db.common.agent.interaction.ParentshipAgent;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.util.HashMap;

import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP_PARENT;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;

public class GraknInsertParentShipAction extends InsertParentShipAction<GraknDbOperationController.TransactionalDbOperation, ConceptMap> {
    public GraknInsertParentShipAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, HashMap<ParentshipAgent.SpouseType, String> marriage, String childEmail) {
        super(dbOperation, marriage, childEmail);
    }

    @Override
    public ConceptMap run() {
        // Parentship where parents have multiple children is represented as multiple ternary relations, each with
        // both parents and one child. They had these children at the same time, and will not have any subsequently.
        Statement parentship = Graql.var(PARENTSHIP);
        Statement child = Graql.var("child");
        Statement mother = Graql.var("mother");
        Statement father = Graql.var("father");

        GraqlInsert parentshipQuery = Graql.match(
                mother.isa(PERSON).has(EMAIL, marriage.get(ParentshipAgent.SpouseType.WIFE)),
                father.isa(PERSON).has(EMAIL, marriage.get(ParentshipAgent.SpouseType.HUSBAND)),
                child.isa(PERSON).has(EMAIL, childEmail)
        ).insert(
                parentship.isa(PARENTSHIP)
                        .rel(PARENTSHIP_PARENT, father)
                        .rel(PARENTSHIP_PARENT, mother)
                        .rel(PARENTSHIP_CHILD, child)
        );
        return Action.singleResult(dbOperation.tx().execute(parentshipQuery));

    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object> () {
            {
                put(InsertParentShipActionField.WIFE_EMAIL, dbOperation.tx().getOnlyAttributeOfThing(answer, "mother", EMAIL));
                put(InsertParentShipActionField.HUSBAND_EMAIL, dbOperation.tx().getOnlyAttributeOfThing(answer, "father", EMAIL));
                put(InsertParentShipActionField.CHILD_EMAIL, dbOperation.tx().getOnlyAttributeOfThing(answer, "child", EMAIL));
            }
        };
    }

    public enum InsertParentShipActionField implements ComparableField {
        WIFE_EMAIL, HUSBAND_EMAIL, CHILD_EMAIL
    }
}
