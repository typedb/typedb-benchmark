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

package grakn.benchmark.grakn.driver;

import grakn.benchmark.simulation.driver.Transaction;
import grakn.client.api.answer.ConceptMap;
import grakn.client.api.query.QueryManager;
import graql.lang.query.GraqlMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class GraknTransaction implements Transaction {

    private static final Logger LOG = LoggerFactory.getLogger(GraknTransaction.class);

    private final grakn.client.api.GraknTransaction tx;

    public GraknTransaction(grakn.client.api.GraknTransaction tx) {
        this.tx = tx;
    }

    public QueryManager query() {
        return tx.query();
    }

    @Override
    public void close() {
        tx.close();
    }

    @Override
    public void commit() {
        tx.commit();
    }

    public <T> List<T> sortedExecute(GraqlMatch query, String attributeName, Integer limit) {
        Stream<T> answerStream = tx.query().match(query)
                .map(conceptMap -> (T) conceptMap.get(attributeName).asThing().asAttribute().getValue())
                .sorted();
        if (limit != null) {
            answerStream = answerStream.limit(limit);
        }
        return answerStream.collect(toList());
    }

    public Object getOnlyAttributeOfThing(ConceptMap answer, String var, String attributeType) {
        return answer.get(var).asThing().asRemote(tx).asThing()
                .getHas(tx.concepts().getAttributeType(attributeType))
                .collect(toList()).get(0).getValue();
    }

    public Object getValueOfAttribute(ConceptMap answer, String varName) {
        return answer.get(varName).asThing().asAttribute().getValue();
    }
}
