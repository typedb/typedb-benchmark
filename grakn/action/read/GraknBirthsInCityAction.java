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

import grakn.simulation.common.action.read.BirthsInCityAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlMatch;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.grakn.action.Model.BORN_IN;
import static grakn.simulation.grakn.action.Model.BORN_IN_CHILD;
import static grakn.simulation.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.DATE_OF_BIRTH;
import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknBirthsInCityAction extends BirthsInCityAction<GraknOperation> {
    public GraknBirthsInCityAction(GraknOperation dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation, city, today);
    }

    @Override
    public List<String> run() {
        GraqlMatch.Unfiltered childrenQuery = query(worldCity.name(), today);
        return dbOperation.sortedExecute(childrenQuery, EMAIL, null);
    }

    public static GraqlMatch.Unfiltered query(String worldCityName, LocalDateTime today) {
        return Graql.match(
                    Graql.var("c").isa(CITY)
                            .has(LOCATION_NAME, worldCityName),
                    Graql.var("child").isa(PERSON)
                            .has(EMAIL, Graql.var(EMAIL))
                            .has(DATE_OF_BIRTH, today),
                    Graql.var("bi")
                            .rel(BORN_IN_PLACE_OF_BIRTH, "c")
                            .rel(BORN_IN_CHILD, "child")
                            .isa(BORN_IN)
            );
    }
}
