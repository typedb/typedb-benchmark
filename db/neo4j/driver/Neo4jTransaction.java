package grakn.simulation.db.neo4j.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.driver.DbTransaction;
import grakn.simulation.db.common.driver.LogWrapper;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.TransientException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.EXECUTE;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.OPEN_TRANSACTION;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.STREAM_AND_SORT;

public class Neo4jTransaction implements DbTransaction {

    private final Session session;
    private final LogWrapper log;
    private final String tracker;
    private org.neo4j.driver.Transaction tx;
    private final List<Query> queries = new ArrayList<Query>();

    public Neo4jTransaction(Session session, LogWrapper log, String tracker) {
        this.session = session;
        this.log = log;
        this.tracker = tracker;
        this.tx = newTransaction();
    }

    private org.neo4j.driver.Transaction newTransaction() {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_TRANSACTION.getName())) {
            return session.beginTransaction();
        }
    }

    public void close() {
        tx.close();
    }

    public void commit() {
        Throwable txEx = null;
        int RETRIES = 5;
        int BACKOFF = 100;
        for (int i = 0; i < RETRIES; i++) {
            try {
                tx.commit();
                return;
            } catch (Throwable ex) {
                txEx = ex;

                // Add whatever exceptions to retry on here
                if (!(ex instanceof TransientException)) {
                    throw ex;
                }
            }

            // Wait so that we don't immediately get into the same deadlock
            if (i < RETRIES - 1) {
                try {
                    tx = newTransaction();
                    replay();
                    Thread.sleep(BACKOFF);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted", e);
                }
            } else {
                throw new RuntimeException("Exceeded the number of retries", txEx);
            }
        }
        throw ((RuntimeException) txEx);
    }

    void addQuery(Query query) {
        this.queries.add(query);
    }

    private void replay() {
        queries.forEach(q -> tx.run(q));
    }

    public List<Record> execute(Query query) {
        addQuery(query);
        log.query(tracker, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
            return tx.run(query).list();
        }
    }

    public <T> List<T> getOrderedAttribute(Query query, String attributeName, Integer limit) {
        List<T> result;
        log.query(tracker, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(STREAM_AND_SORT.getName())) {
            Stream<T> answerStream = execute(query).stream()
                    .map(record -> (T) record.asMap().get(attributeName))
                    .sorted();
            if (limit != null) {
                answerStream = answerStream.limit(limit);
            }
            result = answerStream.collect(Collectors.toList());
        }
        return result;
    }

    public int count(Query countQuery) {
        AtomicReference<Integer> count = new AtomicReference<>(null);
        log.query(tracker, countQuery);
        tx.run(countQuery).single().values().forEach(v -> {
            if (count.get() == null) {
                count.set(v.asInt());
            } else if (count.get() != v.asInt()) {
                throw new RuntimeException("Not all returned counts were the same");
            }
        });
        return tx.run(countQuery).single().get(0).asInt();
    }
}
