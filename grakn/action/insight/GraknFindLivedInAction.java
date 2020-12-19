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

package grakn.simulation.grakn.action.insight;

import grakn.simulation.common.action.insight.FindLivedInAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlMatch;

import java.util.List;

import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PERSON;
import static grakn.simulation.grakn.action.Model.RESIDENCY;
import static grakn.simulation.grakn.action.Model.RESIDENCY_LOCATION;
import static grakn.simulation.grakn.action.Model.RESIDENCY_RESIDENT;

public class GraknFindLivedInAction extends FindLivedInAction<GraknOperation> {
    public GraknFindLivedInAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(query(), EMAIL, null);
    }

    public static GraqlMatch.Unfiltered query() {
        return Graql.match(
                    Graql.var(CITY).isa(CITY)
                            .has(LOCATION_NAME, "Berlin"),
                    Graql.var(RESIDENCY)
                            .rel(RESIDENCY_LOCATION, Graql.var(CITY))
                            .rel(RESIDENCY_RESIDENT, Graql.var(PERSON))
                            .isa(RESIDENCY),
                    Graql.var(PERSON).isa(PERSON).has(EMAIL, Graql.var(EMAIL))
            );
    }
}
