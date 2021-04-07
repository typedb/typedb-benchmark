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
import grakn.benchmark.simulation.action.read.CompaniesInContinentAction;
import grakn.benchmark.simulation.common.World;
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
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknCompaniesInContinentAction extends CompaniesInContinentAction<GraknTransaction> {

    public GraknCompaniesInContinentAction(GraknTransaction tx, World.Continent continent) {
        super(tx, continent);
    }

    @Override
    public List<Long> run() {
        GraqlMatch.Unfiltered query = query(continent.name());
        return tx.sortedExecute(query, COMPANY_NUMBER, null);
    }

    public static GraqlMatch.Unfiltered query(String continentName) {
        return match(
                var(CONTINENT).isa(CONTINENT)
                        .has(LOCATION_NAME, continentName),
                var(LOCATION_HIERARCHY).rel(COUNTRY).rel(CONTINENT).isa(LOCATION_HIERARCHY),
                var(COUNTRY).isa(COUNTRY),
                var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NUMBER, var(COMPANY_NUMBER)),
                var(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, var(COUNTRY))
                        .isa(INCORPORATION)
        );
    }
}
