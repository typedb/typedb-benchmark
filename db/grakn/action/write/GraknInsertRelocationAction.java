package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.write.InsertRelocationAction;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION_DATE;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION_NEW_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION_PREVIOUS_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION_RELOCATED_PERSON;

public class GraknInsertRelocationAction extends InsertRelocationAction<GraknDbOperationController.TransactionalDbOperation, ConceptMap> {
    public GraknInsertRelocationAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, World.City city, LocalDateTime today, String relocateeEmail, String relocationCityName) {
        super(dbOperation, city, today, relocateeEmail, relocationCityName);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert relocatePersonQuery = Graql.match(
                Graql.var(PERSON).isa(PERSON).has(EMAIL, relocateeEmail),
                Graql.var("new-city").isa(CITY).has(LOCATION_NAME, relocationCityName),
                Graql.var("old-city").isa(CITY).has(LOCATION_NAME, city.name())
        ).insert(
                Graql.var(RELOCATION).isa(RELOCATION)
                        .rel(RELOCATION_PREVIOUS_LOCATION, "old-city")
                        .rel(RELOCATION_NEW_LOCATION, "new-city")
                        .rel(RELOCATION_RELOCATED_PERSON, PERSON)
                        .has(RELOCATION_DATE, today)
        );
        return Action.singleResult(dbOperation.tx().execute(relocatePersonQuery));
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertRelocationActionField.PERSON_EMAIL, dbOperation.tx().getOnlyAttributeOfThing(answer, PERSON, EMAIL));
            put(InsertRelocationActionField.NEW_CITY_NAME, dbOperation.tx().getOnlyAttributeOfThing(answer, "new-city", LOCATION_NAME));
            put(InsertRelocationActionField.RELOCATION_DATE, dbOperation.tx().getOnlyAttributeOfThing(answer, RELOCATION, RELOCATION_DATE));
        }};
    }
}
