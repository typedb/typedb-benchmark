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

package grakn.simulation.grakn.action.write;

import grakn.client.concept.answer.ConceptMap;
import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.write.InsertMarriageAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.pattern.variable.ThingVariable;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlInsert;

import java.util.HashMap;

import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.LOCATES;
import static grakn.simulation.grakn.action.Model.LOCATES_LOCATED;
import static grakn.simulation.grakn.action.Model.LOCATES_LOCATION;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.MARRIAGE;
import static grakn.simulation.grakn.action.Model.MARRIAGE_HUSBAND;
import static grakn.simulation.grakn.action.Model.MARRIAGE_ID;
import static grakn.simulation.grakn.action.Model.MARRIAGE_WIFE;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknInsertMarriageAction extends InsertMarriageAction<GraknOperation, ConceptMap> {

    public GraknInsertMarriageAction(GraknOperation dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        super(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert marriageQuery = query(worldCity.name(), marriageIdentifier, wifeEmail, husbandEmail);
        return Action.singleResult(dbOperation.execute(marriageQuery));
    }

    private GraqlInsert query(String worldCityName, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        UnboundVariable husband = Graql.var("husband");
        UnboundVariable wife = Graql.var("wife");
        UnboundVariable city = Graql.var(CITY);
        UnboundVariable marriage = Graql.var("marriage");

        ThingVariable.Attribute cityNameVar = Graql.var().eq(worldCityName);
        ThingVariable.Attribute marriageIdentifierVar = Graql.var().eq(marriageIdentifier);
        ThingVariable.Attribute husbandEmailVar = Graql.var().eq(husbandEmail);
        ThingVariable.Attribute wifeEmailVar = Graql.var().eq(wifeEmail);

        return Graql.match(
                husband.isa(PERSON).has(EMAIL, husbandEmailVar.toString()),
                wife.isa(PERSON).has(EMAIL, wifeEmailVar.toString()),
                city.isa(CITY).has(LOCATION_NAME, cityNameVar.toString())
        ).insert(
                marriage
                    .rel(MARRIAGE_HUSBAND, husband)
                    .rel(MARRIAGE_WIFE, wife)
                    .isa(MARRIAGE)
                    .has(MARRIAGE_ID, marriageIdentifierVar.toString()),
                Graql.var().rel(LOCATES_LOCATED, marriage).rel(LOCATES_LOCATION, city).isa(LOCATES)
        );
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>(){{
            put(InsertMarriageActionField.MARRIAGE_IDENTIFIER, dbOperation.getOnlyAttributeOfThing(answer, "marriage", MARRIAGE_ID));
            put(InsertMarriageActionField.WIFE_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "wife", EMAIL));
            put(InsertMarriageActionField.HUSBAND_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, "husband", EMAIL));
            put(InsertMarriageActionField.CITY_NAME, dbOperation.getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
        }};
    }
}
