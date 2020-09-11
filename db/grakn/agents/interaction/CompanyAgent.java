package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import static grakn.simulation.db.grakn.schema.Schema.COMPANY;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY_NAME;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.COUNTRY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_INCORPORATION;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION_INCORPORATED;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION_INCORPORATING;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_HIERARCHY;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;

public class CompanyAgent extends grakn.simulation.db.common.agents.interaction.CompanyAgent<GraknContext> {

    private Transaction tx;

    @Override
    protected void openTx() {
        if (tx == null) {
            tx = backendContext().tx(getSessionKey());
        }
    }

    @Override
    protected void closeTx() {
        tx.close();
        tx = null;
    }

    @Override
    protected void commitTx() {
        tx.commit();
        tx = null;
    }

    @Override
    protected void insertCompany(int companyNumber, String companyName) {

        GraqlInsert query =
                Graql.match(
                        Graql.var(COUNTRY).isa(COUNTRY)
                                .has(LOCATION_NAME, country().name()))
                        .insert(Graql.var(COMPANY).isa(COMPANY)
                                        .has(COMPANY_NAME, companyName)
                                        .has(COMPANY_NUMBER, companyNumber),
                                Graql.var(INCORPORATION).isa(INCORPORATION)
                                        .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                                        .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
                                        .has(DATE_OF_INCORPORATION, today())
                        );
        log().query("insertCompany", query);
        tx.execute(query);
    }

    static GraqlGet getCompanyNumbersInCountryQuery(World.Country country) {
        return Graql.match(
                Graql.var(COUNTRY).isa(COUNTRY)
                        .has(LOCATION_NAME, country.name()),
                Graql.var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NUMBER, Graql.var(COMPANY_NUMBER)),
                Graql.var(INCORPORATION).isa(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
        ).get();
    }

    static GraqlGet getCompanyNumbersInContinentQuery(World.Continent continent) {
        return Graql.match(
                Graql.var(CONTINENT).isa(CONTINENT)
                        .has(LOCATION_NAME, continent.name()),
                Graql.var(LOCATION_HIERARCHY).isa(LOCATION_HIERARCHY).rel(COUNTRY).rel(CONTINENT),
                Graql.var(COUNTRY).isa(COUNTRY),
                Graql.var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NUMBER, Graql.var(COMPANY_NUMBER)),
                Graql.var(INCORPORATION).isa(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
        ).get();
    }

    @Override
    protected int checkCount() {
        GraqlGet.Aggregate countQuery = Graql.match(
                Graql.var(COUNTRY).isa(COUNTRY)
                        .has(LOCATION_NAME, country().name()),
                Graql.var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NAME, Graql.var(COMPANY_NAME))
                        .has(COMPANY_NUMBER, Graql.var(COMPANY_NUMBER)),
                Graql.var(INCORPORATION).isa(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
                        .has(DATE_OF_INCORPORATION, Graql.var(DATE_OF_INCORPORATION))
        ).get().count();
        log().query("checkCount", countQuery);
        return tx.count(countQuery);
    }
}