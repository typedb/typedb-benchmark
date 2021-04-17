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
import grakn.client.api.answer.Numeric;
import grakn.client.api.query.QueryManager;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class GraknTransaction implements Transaction {

    private static final Logger LOG = LoggerFactory.getLogger(GraknTransaction.class);

    private final grakn.client.api.GraknTransaction tx;
    private final String tracker;
    private final long iteration;
    private boolean closed = false;

    public GraknTransaction(grakn.client.api.GraknTransaction tx, String tracker, long iteration) {
        this.tx = tx;
        this.tracker = tracker;
        this.iteration = iteration;
    }

    public QueryManager query() {
        return tx.query();
    }

    @Override
    public void close() {
        tx.close();
        closed = true;
    }

    @Override
    public void commit() {
        throwIfClosed();
        tx.commit();
        closed = true;
    }

    private void throwIfClosed() {
        if (closed) {
            throw new RuntimeException("Transaction is closed, please open a new one.");
        }
    }

    public <T> List<T> sortedExecute(GraqlMatch query, String attributeName, Integer limit) {
        throwIfClosed();
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        Stream<T> answerStream = tx.query().match(query)
                .map(conceptMap -> (T) conceptMap.get(attributeName).asThing().asAttribute().getValue())
                .sorted();
        if (limit != null) {
            answerStream = answerStream.limit(limit);
        }
        return answerStream.collect(toList());
    }

    public void execute(GraqlDelete query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        tx.query().delete(query).get();
    }

    public void executeAsync(GraqlDelete query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        tx.query().delete(query);
    }

    public List<ConceptMap> execute(GraqlInsert query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        return tx.query().insert(query).collect(toList());
    }

    public Stream<ConceptMap> executeAsync(GraqlInsert query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        return tx.query().insert(query);
    }

    public Stream<ConceptMap> executeAsync(GraqlUpdate query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        return tx.query().update(query);
    }

    public List<ConceptMap> execute(GraqlMatch query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        return tx.query().match(query).collect(toList());
    }

    public Stream<ConceptMap> executeAsync(GraqlMatch query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        return tx.query().match(query);
    }

    public Numeric execute(GraqlMatch.Aggregate query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        return tx.query().match(query).get();
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
