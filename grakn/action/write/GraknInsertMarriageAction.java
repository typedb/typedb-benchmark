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

package grakn.benchmark.grakn.action.write;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.write.InsertMarriageAction;
import grakn.benchmark.simulation.common.World;
import grakn.client.api.answer.ConceptMap;
import graql.lang.pattern.variable.ThingVariable;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlInsert;

import java.util.HashMap;

import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.LOCATES;
import static grakn.benchmark.grakn.action.Model.LOCATES_LOCATED;
import static grakn.benchmark.grakn.action.Model.LOCATES_LOCATION;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.MARRIAGE;
import static grakn.benchmark.grakn.action.Model.MARRIAGE_HUSBAND;
import static grakn.benchmark.grakn.action.Model.MARRIAGE_ID;
import static grakn.benchmark.grakn.action.Model.MARRIAGE_WIFE;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknInsertMarriageAction extends InsertMarriageAction<GraknTransaction, ConceptMap> {

    public GraknInsertMarriageAction(GraknTransaction tx, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        super(tx, city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert marriageQuery = query(worldCity.name(), marriageIdentifier, wifeEmail, husbandEmail);
        return Action.singleResult(tx.execute(marriageQuery));
    }

    private GraqlInsert query(String worldCityName, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        UnboundVariable husband = var("husband");
        UnboundVariable wife = var("wife");
        UnboundVariable city = var(CITY);
        UnboundVariable marriage = var("marriage");

        ThingVariable.Attribute cityNameVar = var().eq(worldCityName);
        ThingVariable.Attribute marriageIdentifierVar = var().eq(marriageIdentifier);
        ThingVariable.Attribute husbandEmailVar = var().eq(husbandEmail);
        ThingVariable.Attribute wifeEmailVar = var().eq(wifeEmail);

        return match(
                husband.isa(PERSON).has(EMAIL, husbandEmailVar.toString()),
                wife.isa(PERSON).has(EMAIL, wifeEmailVar.toString()),
                city.isa(CITY).has(LOCATION_NAME, cityNameVar.toString())
        ).insert(
                marriage
                        .rel(MARRIAGE_HUSBAND, husband)
                        .rel(MARRIAGE_WIFE, wife)
                        .isa(MARRIAGE)
                        .has(MARRIAGE_ID, marriageIdentifierVar.toString()),
                var().rel(LOCATES_LOCATED, marriage).rel(LOCATES_LOCATION, city).isa(LOCATES)
        );
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertMarriageActionField.MARRIAGE_IDENTIFIER, tx.getOnlyAttributeOfThing(answer, "marriage", MARRIAGE_ID));
            put(InsertMarriageActionField.WIFE_EMAIL, tx.getOnlyAttributeOfThing(answer, "wife", EMAIL));
            put(InsertMarriageActionField.HUSBAND_EMAIL, tx.getOnlyAttributeOfThing(answer, "husband", EMAIL));
            put(InsertMarriageActionField.CITY_NAME, tx.getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
        }};
    }
}
