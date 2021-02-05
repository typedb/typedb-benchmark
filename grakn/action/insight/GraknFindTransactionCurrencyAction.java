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

import grakn.benchmark.common.action.insight.FindTransactionCurrencyAction;
import grakn.benchmark.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlMatch;

import java.util.List;
import java.util.stream.Collectors;

import static grakn.benchmark.grakn.action.Model.CURRENCY;
import static grakn.benchmark.grakn.action.Model.TRANSACTION;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknFindTransactionCurrencyAction extends FindTransactionCurrencyAction<GraknOperation> {
    public GraknFindTransactionCurrencyAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.execute(query()).stream().map(ans -> ans.get(CURRENCY).asThing().asAttribute().getValue().toString()).collect(Collectors.toList());
    }

    public static GraqlMatch.Unfiltered query() {
        return match(
                var(TRANSACTION).isa(TRANSACTION).has(CURRENCY, var(CURRENCY))
        );
    }
}
