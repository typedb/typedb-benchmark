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

import grakn.benchmark.common.action.read.CompaniesInContinentAction;
import grakn.benchmark.common.world.World;
import grakn.benchmark.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlMatch;

import java.util.List;

import static grakn.benchmark.grakn.action.Model.COMPANY;
import static grakn.benchmark.grakn.action.Model.COMPANY_NUMBER;
import static grakn.benchmark.grakn.action.Model.CONTINENT;
import static grakn.benchmark.grakn.action.Model.COUNTRY;
import static grakn.benchmark.grakn.action.Model.INCORPORATION;
import static grakn.benchmark.grakn.action.Model.INCORPORATION_INCORPORATED;
import static grakn.benchmark.grakn.action.Model.INCORPORATION_INCORPORATING;
import static grakn.benchmark.grakn.action.Model.LOCATION_HIERARCHY;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;

public class GraknCompaniesInContinentAction extends CompaniesInContinentAction<GraknOperation> {
    public GraknCompaniesInContinentAction(GraknOperation dbOperation, World.Continent continent) {
        super(dbOperation, continent);
    }

    @Override
    public List<Long> run() {
        GraqlMatch.Unfiltered query = query(continent.name());
        return dbOperation.sortedExecute(query, COMPANY_NUMBER, null);
    }

    public static GraqlMatch.Unfiltered query(String continentName) {
        return Graql.match(
                Graql.var(CONTINENT).isa(CONTINENT)
                        .has(LOCATION_NAME, continentName),
                Graql.var(LOCATION_HIERARCHY).rel(COUNTRY).rel(CONTINENT).isa(LOCATION_HIERARCHY),
                Graql.var(COUNTRY).isa(COUNTRY),
                Graql.var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NUMBER, Graql.var(COMPANY_NUMBER)),
                Graql.var(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
                        .isa(INCORPORATION)
        );
    }
}
