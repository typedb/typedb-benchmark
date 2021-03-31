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

package grakn.benchmark.grakn.driver;

import grakn.benchmark.common.driver.LogWrapper;
import grakn.benchmark.common.driver.TransactionalDbOperation;
import grakn.client.api.GraknSession;
import grakn.client.api.GraknTransaction;
import grakn.client.api.answer.ConceptMap;
import grakn.client.api.answer.Numeric;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlUpdate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getOnlyElement;

public class GraknOperation extends TransactionalDbOperation {

    private final GraknTransaction transaction;
    private final LogWrapper log;

    boolean closed = false;

    public GraknOperation(GraknSession session, LogWrapper log, String tracker, long iteration, boolean trace) {
        super(tracker, iteration, trace);
        this.transaction = session.transaction(GraknTransaction.Type.WRITE);
        this.log = log;
    }

    @Override
    public void close() {
        transaction.close();
        closed = true;
    }

    @Override
    public void save() {
        throwIfClosed();
        transaction.commit();
        closed = true;
    }

    private void throwIfClosed() {
        if (closed) {
            throw new RuntimeException("Transaction is closed, please open a new one.");
        }
    }

    public <T> List<T> sortedExecute(GraqlMatch query, String attributeName, Integer limit) {
        throwIfClosed();
        log.query(tracker, iteration, query);
        Stream<T> answerStream = transaction.query().match(query)
                .map(conceptMap -> (T) conceptMap.get(attributeName).asThing().asAttribute().getValue())
                .sorted();
        if (limit != null) {
            answerStream = answerStream.limit(limit);
        }
        return answerStream.collect(Collectors.toList());
    }

    public void execute(GraqlDelete query) {
        log.query(tracker, iteration, query);
        transaction.query().delete(query).get();
    }

    public void executeAsync(GraqlDelete query) {
        log.query(tracker, iteration, query);
        transaction.query().delete(query);
    }

    public List<ConceptMap> execute(GraqlInsert query) {
        log.query(tracker, iteration, query);
        return transaction.query().insert(query).collect(Collectors.toList());
    }

    public Stream<ConceptMap> executeAsync(GraqlInsert query) {
        log.query(tracker, iteration, query);
        return transaction.query().insert(query);
    }

    public Stream<ConceptMap> executeAsync(GraqlUpdate query) {
        log.query(tracker, iteration, query);
        return transaction.query().update(query);
    }

    public List<ConceptMap> execute(GraqlMatch query) {
        log.query(tracker, iteration, query);
        return transaction.query().match(query).collect(Collectors.toList());
    }

    public Stream<ConceptMap> executeAsync(GraqlMatch query) {
        log.query(tracker, iteration, query);
        return transaction.query().match(query);
    }

    public Numeric execute(GraqlMatch.Aggregate query) {
        log.query(tracker, iteration, query);
        return transaction.query().match(query).get();
    }

    public Object getOnlyAttributeOfThing(ConceptMap answer, String varName, String attributeType) {
        return getOnlyElement(answer.get(varName).asThing().asRemote(transaction).asThing().getHas(transaction.concepts().getAttributeType(attributeType)).collect(Collectors.toList())).getValue();
    }

    public Object getValueOfAttribute(ConceptMap answer, String varName) {
        return answer.get(varName).asThing().asAttribute().getValue();
    }
}
