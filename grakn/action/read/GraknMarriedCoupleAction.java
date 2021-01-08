/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.grakn.action.read;

import grakn.benchmark.common.action.SpouseType;
import grakn.benchmark.common.action.read.MarriedCoupleAction;
import grakn.benchmark.common.world.World;
import grakn.benchmark.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlMatch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.LOCATES;
import static grakn.benchmark.grakn.action.Model.LOCATES_LOCATED;
import static grakn.benchmark.grakn.action.Model.LOCATES_LOCATION;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.MARRIAGE;
import static grakn.benchmark.grakn.action.Model.MARRIAGE_HUSBAND;
import static grakn.benchmark.grakn.action.Model.MARRIAGE_ID;
import static grakn.benchmark.grakn.action.Model.MARRIAGE_WIFE;
import static grakn.benchmark.grakn.action.Model.PARENTSHIP;
import static grakn.benchmark.grakn.action.Model.PARENTSHIP_PARENT;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static java.util.stream.Collectors.toList;

public class GraknMarriedCoupleAction extends MarriedCoupleAction<GraknOperation> {
    public GraknMarriedCoupleAction(GraknOperation dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation, city, today);
    }

    @Override
    public List<HashMap<SpouseType, String>> run() {
        GraqlMatch.Sorted marriageQuery = query(city.name());
        return dbOperation.execute(marriageQuery)
                .stream()
                .map(a -> new HashMap<SpouseType, String>() {{
                    put(SpouseType.WIFE, a.get("wife-email").asThing().asAttribute().getValue().toString());
                    put(SpouseType.HUSBAND, a.get("husband-email").asThing().asAttribute().getValue().toString());
                }})
                .collect(toList());
    }

    public static GraqlMatch.Sorted query(String cityName) {
        return Graql.match(
                Graql.var(CITY).isa(CITY)
                        .has(LOCATION_NAME, cityName),
                Graql.var("m")
                        .rel(MARRIAGE_HUSBAND, Graql.var("husband"))
                        .rel(MARRIAGE_WIFE, Graql.var("wife"))
                        .isa(MARRIAGE)
                        .has(MARRIAGE_ID, Graql.var(MARRIAGE_ID)),
                Graql.not(
                        Graql.var("par")
                                .rel(PARENTSHIP_PARENT, "husband")
                                .rel(PARENTSHIP_PARENT, "wife")
                                .isa(PARENTSHIP)
                ),
                Graql.var("husband").isa(PERSON)
                        .has(EMAIL, Graql.var("husband-email")),
                Graql.var("wife").isa(PERSON)
                        .has(EMAIL, Graql.var("wife-email")),
                Graql.var()
                        .rel(LOCATES_LOCATED, Graql.var("m"))
                        .rel(LOCATES_LOCATION, Graql.var(CITY))
                        .isa(LOCATES)
        ).sort(MARRIAGE_ID);
    }


}
