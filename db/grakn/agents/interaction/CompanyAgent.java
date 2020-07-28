package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.world.World;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

public class CompanyAgent extends grakn.simulation.db.common.agents.interaction.CompanyAgent {

    @Override
    protected void insertCompany(int companyNumber, String companyName) {

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
        tx().forGrakn().execute(query);
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

    static GraqlGet getCompanyNumbersInContinentQuery(World.Continent continent) {
        return Graql.match(
                Graql.var("continent").isa("continent")
                        .has("location-name", continent.name()),
                Graql.var("lh").isa("location-hierarchy").rel("country").rel("continent"),
                Graql.var("country").isa("country"),
                Graql.var("company").isa("company")
                        .has("company-number", Graql.var("company-number")),
                Graql.var("reg").isa("incorporation")
                        .rel("incorporation_incorporated", Graql.var("company"))
                        .rel("incorporation_incorporating", Graql.var("country"))
        ).get();
    }

    @Override
    protected int checkCount() {
        GraqlGet.Aggregate countQuery = Graql.match(
                Graql.var("country").isa("country")
                        .has("location-name", country().name()),
                Graql.var("company").isa("company")
                        .has("company-name", Graql.var("companyName"))
                        .has("company-number", Graql.var("companyNumber")),
                Graql.var("reg").isa("incorporation")
                        .rel("incorporation_incorporated", Graql.var("company"))
                        .rel("incorporation_incorporating", Graql.var("country"))
                        .has("date-of-incorporation", today())
        ).get().count();
        return tx().forGrakn().execute(countQuery).get().get(0).number().intValue();
    }
}