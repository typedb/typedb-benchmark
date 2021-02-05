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

package grakn.benchmark.grakn.action.insight;

import grakn.benchmark.common.action.insight.ArbitraryOneHopAction;
import grakn.benchmark.grakn.driver.GraknOperation;
import grakn.client.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlMatch;

import java.util.List;

import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknArbitraryOneHopAction extends ArbitraryOneHopAction<GraknOperation> {
    public GraknArbitraryOneHopAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public Integer run() {
        List<ConceptMap> results = dbOperation.execute(query());
        return null;
    }

    public static GraqlMatch.Filtered query() {
        return match(
                var(PERSON).isa(PERSON).has(EMAIL, PERSON_EMAIL_FOR_QUERY),
                var().rel(var(PERSON)).rel(var("x"))
        ).get("x");
    }
}
