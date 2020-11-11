package grakn.simulation.db.grakn.action.read;

import grakn.simulation.common.action.SpouseType;
import grakn.simulation.common.action.read.MarriedCoupleAction;
import grakn.simulation.common.world.World;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATED;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_HUSBAND;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_ID;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_WIFE;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP_PARENT;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static java.util.stream.Collectors.toList;

public class GraknMarriedCoupleAction extends MarriedCoupleAction<GraknOperation> {
    public GraknMarriedCoupleAction(GraknOperation dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation, city, today);
    }

    @Override
    public List<HashMap<SpouseType, String>> run() {
        GraqlGet.Sorted marriageQuery = query(city.name());
        return dbOperation.execute(marriageQuery)
                .stream()
                .map(a -> new HashMap<SpouseType, String>() {{
                    put(SpouseType.WIFE, a.get("wife-email").asAttribute().value().toString());
                    put(SpouseType.HUSBAND, a.get("husband-email").asAttribute().value().toString());
                }})
                .collect(toList());
    }

    public static GraqlGet.Sorted query(String cityName) {
        return Graql.match(
                    Graql.var(CITY).isa(CITY)
                            .has(LOCATION_NAME, cityName),
                    Graql.var("m").isa(MARRIAGE)
                            .rel(MARRIAGE_HUSBAND, Graql.var("husband"))
                            .rel(MARRIAGE_WIFE, Graql.var("wife"))
                            .has(MARRIAGE_ID, Graql.var(MARRIAGE_ID)),
                    Graql.not(
                            Graql.var("par").isa(PARENTSHIP)
                                    .rel(PARENTSHIP_PARENT, "husband")
                                    .rel(PARENTSHIP_PARENT, "wife")
                    ),
                    Graql.var("husband").isa(PERSON)
                            .has(EMAIL, Graql.var("husband-email")),
                    Graql.var("wife").isa(PERSON)
                            .has(EMAIL, Graql.var("wife-email")),
                    Graql.var().isa(LOCATES)
                            .rel(LOCATES_LOCATED, Graql.var("m"))
                            .rel(LOCATES_LOCATION, Graql.var(CITY))
            ).get().sort(MARRIAGE_ID);
    }


}
