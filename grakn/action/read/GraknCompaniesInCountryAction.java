package grakn.simulation.grakn.action.read;

import grakn.simulation.common.action.read.CompaniesInCountryAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.grakn.action.Model.COMPANY;
import static grakn.simulation.grakn.action.Model.COMPANY_NUMBER;
import static grakn.simulation.grakn.action.Model.COUNTRY;
import static grakn.simulation.grakn.action.Model.INCORPORATION;
import static grakn.simulation.grakn.action.Model.INCORPORATION_INCORPORATED;
import static grakn.simulation.grakn.action.Model.INCORPORATION_INCORPORATING;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;

public class GraknCompaniesInCountryAction extends CompaniesInCountryAction<GraknOperation> {
    public GraknCompaniesInCountryAction(GraknOperation dbOperation, World.Country country, int numCompanies) {
        super(dbOperation, country, numCompanies);
    }

    @Override
    public List<Long> run() {
        GraqlGet.Unfiltered companyNumbersQuery = query(country.name());
        return dbOperation.sortedExecute(companyNumbersQuery, COMPANY_NUMBER, numCompanies);
    }

    public static GraqlGet.Unfiltered query(String countryName) {
        return Graql.match(
                    Graql.var(COUNTRY).isa(COUNTRY)
                            .has(LOCATION_NAME, countryName),
                    Graql.var(COMPANY).isa(COMPANY)
                            .has(COMPANY_NUMBER, Graql.var(COMPANY_NUMBER)),
                    Graql.var(INCORPORATION).isa(INCORPORATION)
                            .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                            .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
            ).get();
    }
}
