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

package grakn.benchmark.grakn.agent;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.agent.CompanyAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import graql.lang.Graql;

import java.time.LocalDateTime;

import static grakn.benchmark.grakn.agent.Types.COMPANY;
import static grakn.benchmark.grakn.agent.Types.COMPANY_NAME;
import static grakn.benchmark.grakn.agent.Types.COMPANY_NUMBER;
import static grakn.benchmark.grakn.agent.Types.COUNTRY;
import static grakn.benchmark.grakn.agent.Types.DATE_OF_INCORPORATION;
import static grakn.benchmark.grakn.agent.Types.INCORPORATION;
import static grakn.benchmark.grakn.agent.Types.INCORPORATION_INCORPORATED;
import static grakn.benchmark.grakn.agent.Types.INCORPORATION_INCORPORATING;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static graql.lang.Graql.var;

public class GraknCompanyAgent extends CompanyAgent<GraknTransaction> {

    public GraknCompanyAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void insertCompany(GraknTransaction tx, GeoData.Country country, LocalDateTime today, int companyNumber, String companyName) {
        tx.execute(Graql.match(
                var(COUNTRY).isa(COUNTRY).has(LOCATION_NAME, country.name())
        ).insert(
                var(COMPANY).isa(COMPANY).has(COMPANY_NAME, companyName).has(COMPANY_NUMBER, companyNumber),
                var(INCORPORATION).rel(INCORPORATION_INCORPORATED, var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, var(COUNTRY))
                        .isa(INCORPORATION).has(DATE_OF_INCORPORATION, today)
        ));
    }

    //    @Override
//    protected HashMap<Action.ComparableField, Object> outputForReport(ConceptMap answer) {
//        return new HashMap<>() {{
//            put(InsertCompanyActionField.COMPANY_NAME, tx.getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NAME));
//            put(InsertCompanyActionField.COMPANY_NUMBER, tx.getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NUMBER));
//            put(InsertCompanyActionField.COUNTRY, tx.getOnlyAttributeOfThing(answer, COUNTRY, LOCATION_NAME));
//            put(InsertCompanyActionField.DATE_OF_INCORPORATION, tx.getOnlyAttributeOfThing(answer, INCORPORATION, DATE_OF_INCORPORATION));
//        }};
//    }
}
