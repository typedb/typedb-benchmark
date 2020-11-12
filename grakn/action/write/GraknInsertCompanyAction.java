package grakn.simulation.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.write.InsertCompanyAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.grakn.action.Model.COMPANY;
import static grakn.simulation.grakn.action.Model.COMPANY_NAME;
import static grakn.simulation.grakn.action.Model.COMPANY_NUMBER;
import static grakn.simulation.grakn.action.Model.COUNTRY;
import static grakn.simulation.grakn.action.Model.DATE_OF_INCORPORATION;
import static grakn.simulation.grakn.action.Model.INCORPORATION;
import static grakn.simulation.grakn.action.Model.INCORPORATION_INCORPORATED;
import static grakn.simulation.grakn.action.Model.INCORPORATION_INCORPORATING;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;

public class GraknInsertCompanyAction extends InsertCompanyAction<GraknOperation, ConceptMap> {

    public GraknInsertCompanyAction(GraknOperation dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        super(dbOperation, country, today, companyNumber, companyName);
    }

    @Override
    public ConceptMap run() {
        return Action.singleResult(dbOperation.execute(query(country.name(), today, companyNumber, companyName)));
    }

    public static GraqlInsert query(String countryName, LocalDateTime today, int companyNumber, String companyName) {
        return Graql.match(
                Graql.var(COUNTRY).isa(COUNTRY)
                        .has(LOCATION_NAME, countryName))
                .insert(Graql.var(COMPANY).isa(COMPANY)
                                .has(COMPANY_NAME, companyName)
                                .has(COMPANY_NUMBER, companyNumber),
                        Graql.var(INCORPORATION).isa(INCORPORATION)
                                .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                                .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
                                .has(DATE_OF_INCORPORATION, today)
                );
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertCompanyActionField.COMPANY_NAME, dbOperation.getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NAME));
                put(InsertCompanyActionField.COMPANY_NUMBER, dbOperation.getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NUMBER));
                put(InsertCompanyActionField.COUNTRY, dbOperation.getOnlyAttributeOfThing(answer, COUNTRY, LOCATION_NAME));
                put(InsertCompanyActionField.DATE_OF_INCORPORATION, dbOperation.getOnlyAttributeOfThing(answer, INCORPORATION, DATE_OF_INCORPORATION));
            }
        };
    }
}
