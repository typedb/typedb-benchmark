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
import grakn.benchmark.simulation.agent.PersonBirthAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import graql.lang.Graql;

import java.time.LocalDateTime;

import static grakn.benchmark.grakn.agent.Types.BORN_IN;
import static grakn.benchmark.grakn.agent.Types.BORN_IN_CHILD;
import static grakn.benchmark.grakn.agent.Types.BORN_IN_PLACE_OF_BIRTH;
import static grakn.benchmark.grakn.agent.Types.CITY;
import static grakn.benchmark.grakn.agent.Types.DATE_OF_BIRTH;
import static grakn.benchmark.grakn.agent.Types.EMAIL;
import static grakn.benchmark.grakn.agent.Types.FORENAME;
import static grakn.benchmark.grakn.agent.Types.GENDER;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static grakn.benchmark.grakn.agent.Types.SURNAME;
import static graql.lang.Graql.var;

public class GraknPersonBirthAgent extends PersonBirthAgent<GraknTransaction> {

    public GraknPersonBirthAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void insertPerson(GraknTransaction tx, GeoData.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        tx.execute(Graql.match(
                var(CITY).isa(CITY).has(LOCATION_NAME, city.name())
        ).insert(
                var(PERSON).isa(PERSON).has(EMAIL, email).has(DATE_OF_BIRTH, today).has(GENDER, gender).has(FORENAME, forename).has(SURNAME, surname),
                var(BORN_IN).rel(BORN_IN_CHILD, var(PERSON)).rel(BORN_IN_PLACE_OF_BIRTH, var(CITY)).isa(BORN_IN)
        ));
    }

    //    @Override
//    protected HashMap<Action.ComparableField, Object> outputForReport(ConceptMap answer) {
//        return new HashMap<>() {{
//            put(InsertPersonActionField.EMAIL, tx.getOnlyAttributeOfThing(answer, PERSON, EMAIL));
//            put(InsertPersonActionField.DATE_OF_BIRTH, tx.getOnlyAttributeOfThing(answer, PERSON, DATE_OF_BIRTH));
//            put(InsertPersonActionField.GENDER, tx.getOnlyAttributeOfThing(answer, PERSON, GENDER));
//            put(InsertPersonActionField.FORENAME, tx.getOnlyAttributeOfThing(answer, PERSON, FORENAME));
//            put(InsertPersonActionField.SURNAME, tx.getOnlyAttributeOfThing(answer, PERSON, SURNAME));
//        }};
//    }
}
