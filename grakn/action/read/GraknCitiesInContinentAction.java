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

package grakn.simulation.grakn.action.read;

import grakn.simulation.common.action.read.CitiesInContinentAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.CONTINENT;
import static grakn.simulation.grakn.action.Model.LOCATION_HIERARCHY;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;

public class GraknCitiesInContinentAction extends CitiesInContinentAction<GraknOperation> {
    public GraknCitiesInContinentAction(GraknOperation dbOperation, World.City city) {
        super(dbOperation, city);
    }

    @Override
    public List<String> run() {
        GraqlGet.Unfiltered relocationCitiesQuery = query(city.name(), city.country().continent().name());
        return dbOperation.sortedExecute(relocationCitiesQuery, "city-name", null);
    }

    public static GraqlGet.Unfiltered query(String cityName, String continentName) {
        return Graql.match(
                    Graql.var(CITY).isa(CITY).has(LOCATION_NAME, Graql.var("city-name")),
                    Graql.var(CONTINENT).isa(CONTINENT).has(LOCATION_NAME, continentName),
                    Graql.var("lh1").isa(LOCATION_HIERARCHY).rel(CITY).rel(CONTINENT),
                    Graql.var("city-name").neq(cityName)
            ).get();
    }
}
