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

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.action.read.BirthsInCityAction;
import grakn.benchmark.simulation.common.World;
import graql.lang.query.GraqlMatch;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.benchmark.grakn.action.Model.BORN_IN;
import static grakn.benchmark.grakn.action.Model.BORN_IN_CHILD;
import static grakn.benchmark.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.DATE_OF_BIRTH;
import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknBirthsInCityAction extends BirthsInCityAction<GraknTransaction> {
    public GraknBirthsInCityAction(GraknTransaction tx, World.City city, LocalDateTime today) {
        super(tx, city, today);
    }

    @Override
    public List<String> run() {
        GraqlMatch.Unfiltered childrenQuery = query(worldCity.name(), today);
        return tx.sortedExecute(childrenQuery, EMAIL, null);
    }

    public static GraqlMatch.Unfiltered query(String worldCityName, LocalDateTime today) {
        return match(
                var("c").isa(CITY)
                        .has(LOCATION_NAME, worldCityName),
                var("child").isa(PERSON)
                        .has(EMAIL, var(EMAIL))
                        .has(DATE_OF_BIRTH, today),
                var("bi")
                        .rel(BORN_IN_PLACE_OF_BIRTH, "c")
                        .rel(BORN_IN_CHILD, "child")
                        .isa(BORN_IN)
        );
    }
}
