package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.write.InsertMarriageAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.StatementAttribute;

import java.util.HashMap;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATED;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_HUSBAND;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_ID;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_WIFE;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;

public class GraknInsertMarriageAction extends InsertMarriageAction<GraknOperation, ConceptMap> {

    public GraknInsertMarriageAction(GraknOperation dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        super(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public ConceptMap run() {
        Statement husband = Graql.var("husband");
        Statement wife = Graql.var("wife");
        Statement city = Graql.var(CITY);
        Statement marriage = Graql.var("marriage");

        StatementAttribute cityNameVar = Graql.var().val(worldCity.name());
        StatementAttribute marriageIdentifierVar = Graql.var().val(marriageIdentifier);
        StatementAttribute husbandEmailVar = Graql.var().val(husbandEmail);
        StatementAttribute wifeEmailVar = Graql.var().val(wifeEmail);

        GraqlInsert marriageQuery = Graql.match(
                husband.isa(PERSON).has(EMAIL, husbandEmailVar),
                wife.isa(PERSON).has(EMAIL, wifeEmailVar),
                city.isa(CITY).has(LOCATION_NAME, cityNameVar)
        ).insert(
                marriage.isa(MARRIAGE)
                        .rel(MARRIAGE_HUSBAND, husband)
                        .rel(MARRIAGE_WIFE, wife)
                        .has(MARRIAGE_ID, marriageIdentifierVar),
                Graql.var().isa(LOCATES).rel(LOCATES_LOCATED, marriage).rel(LOCATES_LOCATION, city)
        );
        return Action.singleResult(dbOperation.execute(marriageQuery));
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>(){{
            put(InsertMarriageActionField.MARRIAGE_IDENTIFIER, dbOperation.getOnlyAttributeOfThing(answer, "marriage", MARRIAGE_ID));
            put(InsertMarriageActionField.WIFE_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "wife", EMAIL));
            put(InsertMarriageActionField.HUSBAND_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "husband", EMAIL));
            put(InsertMarriageActionField.CITY_NAME, dbOperation.getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
        }};

    }
}
