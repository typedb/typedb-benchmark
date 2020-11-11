package grakn.simulation.db.grakn.action.read;

import grakn.simulation.common.action.read.UnmarriedPeopleInCityAction;
import grakn.simulation.common.world.World;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.END_DATE;
import static grakn.simulation.db.grakn.schema.Schema.GENDER;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_HUSBAND;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_WIFE;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_RESIDENT;

public class GraknUnmarriedPeopleInCityAction extends UnmarriedPeopleInCityAction<GraknOperation> {
    public GraknUnmarriedPeopleInCityAction(GraknOperation dbOperation, World.City city, String gender, LocalDateTime dobOfAdults) {
        super(dbOperation, city, gender, dobOfAdults);
    }

    @Override
    public List<String> run() {

        String marriageRole;
        if (gender.equals("female")) {
            marriageRole = MARRIAGE_WIFE;
        } else if (gender.equals("male")) {
            marriageRole = MARRIAGE_HUSBAND;
        } else {
            throw new IllegalArgumentException("Gender must be male or female");
        }
        GraqlGet query = query(marriageRole, gender, dobOfAdults, city.name());
        return dbOperation.sortedExecute(query, EMAIL, null);
    }

    public static GraqlGet query(String marriageRole, String gender, LocalDateTime dobOfAdults, String cityName) {
        Statement personVar = Graql.var(PERSON);
        Statement cityVar = Graql.var(CITY);
        return Graql.match(
                    personVar.isa(PERSON).has(GENDER, gender).has(EMAIL, Graql.var(EMAIL)).has(DATE_OF_BIRTH, Graql.var(DATE_OF_BIRTH)),
                    Graql.var(DATE_OF_BIRTH).lte(dobOfAdults),
                    Graql.not(Graql.var("m").isa(MARRIAGE).rel(marriageRole, personVar)),
                    Graql.var("r").isa(RESIDENCY).rel(RESIDENCY_RESIDENT, personVar).rel(RESIDENCY_LOCATION, cityVar),
                    Graql.not(Graql.var("r").has(END_DATE, Graql.var(END_DATE))),
                    cityVar.isa(CITY).has(LOCATION_NAME, cityName)
            ).get(EMAIL);
    }
}
