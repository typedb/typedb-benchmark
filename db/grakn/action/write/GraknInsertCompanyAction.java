package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.write.InsertCompanyAction;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.db.grakn.schema.Schema.COMPANY;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY_NAME;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.grakn.schema.Schema.COUNTRY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_INCORPORATION;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION_INCORPORATED;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION_INCORPORATING;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;

public class GraknInsertCompanyAction extends InsertCompanyAction<GraknDbOperationController.TransactionalDbOperation, ConceptMap> {

    public GraknInsertCompanyAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        super(dbOperation, country, today, companyNumber, companyName);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert query =
                Graql.match(
                        Graql.var(COUNTRY).isa(COUNTRY)
                                .has(LOCATION_NAME, country.name()))
                        .insert(Graql.var(COMPANY).isa(COMPANY)
                                        .has(COMPANY_NAME, companyName)
                                        .has(COMPANY_NUMBER, companyNumber),
                                Graql.var(INCORPORATION).isa(INCORPORATION)
                                        .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                                        .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
                                        .has(DATE_OF_INCORPORATION, today)
                        );
        return Action.singleResult(dbOperation.tx().execute(query));
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertCompanyActionField.COMPANY_NAME, dbOperation.tx().getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NAME));
                put(InsertCompanyActionField.COMPANY_NUMBER, dbOperation.tx().getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NUMBER));
                put(InsertCompanyActionField.COUNTRY, dbOperation.tx().getOnlyAttributeOfThing(answer, COUNTRY, LOCATION_NAME));
                put(InsertCompanyActionField.DATE_OF_INCORPORATION, dbOperation.tx().getOnlyAttributeOfThing(answer, INCORPORATION, DATE_OF_INCORPORATION));
            }
        };
    }
}
