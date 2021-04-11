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
import grakn.benchmark.simulation.action.read.CompaniesInCountryAction;
import grakn.benchmark.simulation.common.GeoData;
import graql.lang.query.GraqlMatch;

import java.util.List;

import static grakn.benchmark.grakn.action.Model.COMPANY;
import static grakn.benchmark.grakn.action.Model.COMPANY_NUMBER;
import static grakn.benchmark.grakn.action.Model.COUNTRY;
import static grakn.benchmark.grakn.action.Model.INCORPORATION;
import static grakn.benchmark.grakn.action.Model.INCORPORATION_INCORPORATED;
import static grakn.benchmark.grakn.action.Model.INCORPORATION_INCORPORATING;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknCompaniesInCountryAction extends CompaniesInCountryAction<GraknTransaction> {

    public GraknCompaniesInCountryAction(GraknTransaction tx, GeoData.Country country, int numCompanies) {
        super(tx, country, numCompanies);
    }

    @Override
    public List<Long> run() {
        GraqlMatch.Unfiltered companyNumbersQuery = query(country.name());
        return tx.sortedExecute(companyNumbersQuery, COMPANY_NUMBER, numCompanies);
    }

    public static GraqlMatch.Unfiltered query(String countryName) {
        return match(
                var(COUNTRY).isa(COUNTRY)
                        .has(LOCATION_NAME, countryName),
                var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NUMBER, var(COMPANY_NUMBER)),
                var(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, var(COUNTRY))
                        .isa(INCORPORATION)
        );
    }
}
