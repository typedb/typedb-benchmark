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

package grakn.simulation.grakn.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.Grakn;
import grakn.client.concept.answer.ConceptMap;
import grakn.simulation.common.driver.LogWrapper;
import grakn.simulation.common.driver.TransactionalDbOperation;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlInsert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.EXECUTE;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.EXECUTE_ASYNC;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.SORTED_EXECUTE;

public class GraknOperation extends TransactionalDbOperation {

    private final Grakn.Transaction transaction;
    private final LogWrapper log;

    boolean closed = false;

    public GraknOperation(Grakn.Session session, LogWrapper log, String tracker, long iteration, boolean trace) {
        super(tracker, iteration, trace);
        this.transaction = session.transaction(Grakn.Transaction.Type.WRITE);
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

    public <T> List<T> sortedExecute(GraqlMatch query, String attributeName, Integer limit){
        throwIfClosed();
        log.query(tracker, iteration, query);
        return trace(() -> {
            Stream<T> answerStream = transaction.query().match(query)
                    .map(conceptMap -> (T) conceptMap.get(attributeName).asThing().asAttribute().getValue())
                    .sorted();
            if (limit != null) {
                answerStream = answerStream.limit(limit);
            }
            return answerStream.collect(Collectors.toList());
        }, SORTED_EXECUTE.getName());
    }

    public void execute(GraqlDelete query) {
        log.query(tracker, iteration, query);
        trace(() -> transaction.query().delete(query).get(), EXECUTE.getName());
    }

    public void executeAsync(GraqlDelete query) {
        log.query(tracker, iteration, query);
        trace(() -> transaction.query().delete(query), EXECUTE_ASYNC.getName());
    }

    public List<ConceptMap> execute(GraqlInsert query) {
        log.query(tracker, iteration, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
            return transaction.query().insert(query).collect(Collectors.toList());
        }
    }

    public Stream<ConceptMap> executeAsync(GraqlInsert query) {
        log.query(tracker, iteration, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE_ASYNC.getName())) {
            return transaction.query().insert(query);
        }
    }

    public List<ConceptMap> execute(GraqlMatch query) {
        log.query(tracker, iteration, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
            return transaction.query().match(query).collect(Collectors.toList());
        }
    }

    public Stream<ConceptMap> executeAsync(GraqlMatch query) {
        log.query(tracker, iteration, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE_ASYNC.getName())) {
            return transaction.query().match(query);
        }
    }

    public Number execute(GraqlMatch.Aggregate query) {
        log.query(tracker, iteration, query);
//        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
//            return getOnlyElement(transaction.query().match(query).get()).number();
//        }
        throw new RuntimeException("GraqlMatch.Aggregate is not implemented yet."); //todo
    }

    public Object getOnlyAttributeOfThing(ConceptMap answer, String varName, String attributeType) {
        return getOnlyElement(answer.get(varName).asThing().asRemote(transaction).asThing().getHas(transaction.concepts().getAttributeType(attributeType)).collect(Collectors.toList())).getValue();
    }

    public Object getValueOfAttribute(ConceptMap answer, String varName) {
        return answer.get(varName).asThing().asAttribute().getValue();
    }
}
