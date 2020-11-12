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

import grakn.simulation.common.action.insight.FindSpecificPersonAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.stream.Collectors;

import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknFindSpecificPersonAction extends FindSpecificPersonAction<GraknOperation> {
    public GraknFindSpecificPersonAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public String run() {
        return optionalSingleResult(dbOperation.execute(query()).stream().map(ans -> ans.get(EMAIL).asAttribute().value().toString()).collect(Collectors.toList()));
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(PERSON).isa(PERSON).has(EMAIL, Graql.var(EMAIL)),
                    Graql.var(EMAIL).isa(EMAIL).val(PERSON_EMAIL_FOR_QUERY)
            ).get();
    }
}
