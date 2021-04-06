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
import grakn.benchmark.simulation.action.read.ResidentsInCityAction;
import grakn.benchmark.simulation.world.World;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlMatch;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.END_DATE;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static grakn.benchmark.grakn.action.Model.RESIDENCY;
import static grakn.benchmark.grakn.action.Model.RESIDENCY_LOCATION;
import static grakn.benchmark.grakn.action.Model.RESIDENCY_RESIDENT;
import static grakn.benchmark.grakn.action.Model.START_DATE;
import static graql.lang.Graql.match;
import static graql.lang.Graql.not;
import static graql.lang.Graql.var;

public class GraknResidentsInCityAction extends ResidentsInCityAction<GraknTransaction> {

    public GraknResidentsInCityAction(GraknTransaction dbOperation, World.City city, int numResidents, LocalDateTime earliestDate) {
        super(dbOperation, city, numResidents, earliestDate);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(query(city.name(), earliestDate), EMAIL, numResidents);
    }

    public static GraqlMatch.Unfiltered query(String cityName, LocalDateTime earliestDate) {
        UnboundVariable person = var(PERSON);
        UnboundVariable cityVar = var(CITY);
        UnboundVariable residency = var("r");
        UnboundVariable startDate = var(START_DATE);
        UnboundVariable endDate = var(END_DATE);
        return match(
                person.isa(PERSON)
                        .has(EMAIL, var(EMAIL)),
                cityVar
                        .isa(CITY).has(LOCATION_NAME, cityName),
                residency
                        .rel(RESIDENCY_RESIDENT, PERSON)
                        .rel(RESIDENCY_LOCATION, CITY)
                        .isa(RESIDENCY)
                        .has(START_DATE, startDate),
                not(
                        residency
                                .has(END_DATE, endDate)
                ),
                startDate.lte(earliestDate)
        );
    }
}
