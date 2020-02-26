package grakn.simulation.agents;

import grakn.simulation.agents.common.CountryAgent;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import org.apache.commons.lang3.StringUtils;

public class CompanyAgent extends CountryAgent {

    private static final int NUM_COMPANIES = 5;

    @Override
    public void iterate() {
        for (int i = 0; i < NUM_COMPANIES; i++) {
            insertCompany(i);
        }
        tx().commit();
    }

    private void insertCompany(int i) {
        String adjective = pickOne(world().getAdjectives());
        String noun = pickOne(world().getNouns());

        String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun);

        String companyNumberString = companyName
                + i
                + simulationStep() + "_"
                + country().name();
        int companyNumber = companyNumberString.hashCode();

        GraqlInsert query =
                Graql.match(
                        Graql.var("country").isa("country")
                                .has("location-name", country().name()))
                        .insert(Graql.var("company").isa("company")
                                        .has("company-name", companyName)
                                        .has("company-number", companyNumber),
                                Graql.var("reg").isa("incorporation")
                                        .rel("incorporation_incorporated", Graql.var("company"))
                                        .rel("incorporation_incorporating", Graql.var("country"))
                                        .has("date-of-incorporation", today())
                        );
        log().query("insertCompany", query);
        tx().execute(query);
    }

    static GraqlGet getCompanyNumbersInCountryQuery(World.Country country) {
        return Graql.match(
                Graql.var("country").isa("country")
                        .has("location-name", country.name()),
                Graql.var("company").isa("company")
                        .has("company-number", Graql.var("company-number")),
                Graql.var("reg").isa("incorporation")
                        .rel("incorporation_incorporated", Graql.var("company"))
                        .rel("incorporation_incorporating", Graql.var("country"))
        ).get();
    }
}