package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.List;

public class RelocationAgent extends grakn.simulation.db.common.agents.interaction.RelocationAgent {

    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate) {
        return Graql.match(
                Graql.var("person").isa("person").has("email", Graql.var("email")),
                Graql.var("city").isa("city").has("location-name", city.name()),
                Graql.var("r").isa("residency")
                        .rel("residency_resident", "person")
                        .rel("residency_location", "city")
                        .has("start-date", Graql.var("start-date")),
                Graql.not(Graql.var("r").has("end-date", Graql.var("ed"))),
                Graql.var("start-date").lte(earliestDate)
        ).get();
    }

    @Override
    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet.Unfiltered cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        int numRelocations = world().getScaleFactor();
        return ((Transaction)tx()).getOrderedAttribute(cityResidentsQuery, "email", numRelocations);
    }

    @Override
    protected List<String> getRelocationCityNames() {

        GraqlGet.Unfiltered relocationCitiesQuery = Graql.match(
                Graql.var("city").isa("city").has("location-name", Graql.var("city-name")),
                Graql.var("continent").isa("continent").has("location-name", city().country().continent().name()),
                Graql.var("lh1").isa("location-hierarchy").rel("city").rel("continent"),
                Graql.var("city-name").neq(city().name())
        ).get();

        log().query("getRelocationCityNames", relocationCitiesQuery);
        return ((Transaction)tx()).getOrderedAttribute(relocationCitiesQuery, "city-name", null);
    }

    @Override
    protected void insertRelocation(String email, String newCityName) {
        GraqlInsert relocatePersonQuery = Graql.match(
                Graql.var("p").isa("person").has("email", email),
                Graql.var("new-city").isa("city").has("location-name", newCityName),
                Graql.var("old-city").isa("city").has("location-name", city().name())
        ).insert(
                Graql.var("r").isa("relocation")
                        .rel("relocation_previous-location", "old-city")
                        .rel("relocation_new-location", "new-city")
                        .rel("relocation_relocated-person", "p")
                        .has("relocation-date", today())
        );

        log().query("insertRelocation", relocatePersonQuery);
        tx().forGrakn().execute(relocatePersonQuery);
    }
}