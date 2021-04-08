/*
 * Copyright (C) 2021 Grakn Labs
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
import grakn.benchmark.simulation.action.read.CitiesInContinentAction;
import grakn.benchmark.simulation.common.World;
import graql.lang.query.GraqlMatch;

import java.util.List;

import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.CONTINENT;
import static grakn.benchmark.grakn.action.Model.LOCATION_HIERARCHY;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknCitiesInContinentAction extends CitiesInContinentAction<GraknTransaction> {

    public GraknCitiesInContinentAction(GraknTransaction tx, World.City city) {
        super(tx, city);
    }

    @Override
    public List<String> run() {
        GraqlMatch.Unfiltered relocationCitiesQuery = query(city.name(), city.country().continent().name());
        return tx.sortedExecute(relocationCitiesQuery, "city-name", null);
    }

    public static GraqlMatch.Unfiltered query(String cityName, String continentName) {
        return match(
                var(CITY).isa(CITY).has(LOCATION_NAME, var("city-name")),
                var(CONTINENT).isa(CONTINENT).has(LOCATION_NAME, continentName),
                var("lh1").rel(CITY).rel(CONTINENT).isa(LOCATION_HIERARCHY),
                var("city-name").neq(cityName)
        );
    }
}
