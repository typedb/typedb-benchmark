package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.write.InsertPersonAction;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.db.grakn.schema.Schema.BORN_IN;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.FORENAME;
import static grakn.simulation.db.grakn.schema.Schema.GENDER;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.SURNAME;

public class GraknInsertPersonAction extends InsertPersonAction<GraknDbOperationController.TransactionalDbOperation, ConceptMap> {
    public GraknInsertPersonAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        super(dbOperation, city, today, email, gender, forename, surname);
    }

    @Override
    public ConceptMap run() {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var(PERSON);
        Statement bornIn = Graql.var(BORN_IN);
        Statement emailVar = Graql.var(EMAIL);
        Statement genderVar = Graql.var(GENDER);
        Statement forenameVar = Graql.var(FORENAME);
        Statement surnameVar = Graql.var(SURNAME);
        Statement dobVar = Graql.var(DATE_OF_BIRTH);

        GraqlInsert query =
                Graql.match(
                        city.isa(CITY)
                                .has(LOCATION_NAME, worldCity.name()))
                        .insert(
                                person.isa(PERSON)
                                        .has(EMAIL, emailVar)
                                        .has(DATE_OF_BIRTH, dobVar)
                                        .has(GENDER, genderVar)
                                        .has(FORENAME, forenameVar)
                                        .has(SURNAME, surnameVar),
                                bornIn
                                        .isa(BORN_IN)
                                        .rel(BORN_IN_CHILD, person)
                                        .rel(BORN_IN_PLACE_OF_BIRTH, city),
                                emailVar.val(email),
                                genderVar.val(gender),
                                forenameVar.val(forename),
                                surnameVar.val(surname),
                                dobVar.val(today)
                        );
        return Action.singleResult(dbOperation.tx().execute(query));
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>(){
            {
                put(InsertPersonActionField.EMAIL, dbOperation.tx().getOnlyAttributeOfThing(answer, PERSON, EMAIL));
                put(InsertPersonActionField.DATE_OF_BIRTH, dbOperation.tx().getOnlyAttributeOfThing(answer, PERSON, DATE_OF_BIRTH));
                put(InsertPersonActionField.GENDER, dbOperation.tx().getOnlyAttributeOfThing(answer, PERSON, GENDER));
                put(InsertPersonActionField.FORENAME, dbOperation.tx().getOnlyAttributeOfThing(answer, PERSON, FORENAME));
                put(InsertPersonActionField.SURNAME, dbOperation.tx().getOnlyAttributeOfThing(answer, PERSON, SURNAME));
            }};
    }
}
