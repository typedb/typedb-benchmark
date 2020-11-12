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

import grakn.simulation.common.action.insight.FindTransactionCurrencyAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;
import java.util.stream.Collectors;

import static grakn.simulation.grakn.action.Model.CURRENCY;
import static grakn.simulation.grakn.action.Model.TRANSACTION;

public class GraknFindTransactionCurrencyAction extends FindTransactionCurrencyAction<GraknOperation> {
    public GraknFindTransactionCurrencyAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.execute(query()).stream().map(ans -> ans.get(CURRENCY).asAttribute().value().toString()).collect(Collectors.toList());
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(TRANSACTION).isa(TRANSACTION).has(CURRENCY, Graql.var(CURRENCY))
                    ).get();
    }
}
