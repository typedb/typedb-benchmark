package grakn.simulation.db.grakn.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.context.DatabaseTransaction;
import grakn.simulation.db.common.context.LogWrapper;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.EXECUTE;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.STREAM_AND_SORT;

public class Transaction implements DatabaseTransaction {

    boolean closed = false;

    private GraknClient.Transaction transaction;
    private final LogWrapper log;
    private final String tracker;

    public Transaction(GraknClient.Transaction transaction, LogWrapper log, String tracker) {
        this.transaction = transaction;
        this.log = log;
        this.tracker = tracker;
    }

    public void close() {
        throwIfClosed();
        transaction.close();
        closed = true;
    }

    public void commit() {
        throwIfClosed();
        transaction.commit();
        closed = true;
    }

    private void throwIfClosed() {
        if (closed) {
            throw new RuntimeException("Transaction is closed, please open a new one.");
        }
    }

    public <T> List<T> getOrderedAttribute(GraqlGet query, String attributeName, Integer limit){
        throwIfClosed();
        List<T> result;
        log.query(tracker, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(STREAM_AND_SORT.getName())) {
            Stream<T> answerStream = transaction.stream(query).get()
                    .map(conceptMap -> (T) conceptMap.get(attributeName).asAttribute().value())
                    .sorted();
            if (limit != null) {
                answerStream = answerStream.limit(limit);
            }
            result = answerStream.collect(Collectors.toList());
        }
        return result;
    }

    public int count(GraqlGet.Aggregate countQuery) {
        throwIfClosed();
        log.query(tracker, countQuery);
        return getOnlyElement(transaction.execute(countQuery).get()).number().intValue();
    }

    public void execute(GraqlDelete query) {
        log.query(tracker, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
            transaction.execute(query).get();
        }
    }

    public List<ConceptMap> execute(GraqlInsert query) {
        log.query(tracker, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
            return transaction.execute(query).get();
        }
    }

    public List<ConceptMap> execute(GraqlGet query) {
        log.query(tracker, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
            return transaction.execute(query).get();
        }
    }

    public Object getOnlyAttributeOfThing(ConceptMap answer, String varName, String attributeType) {
        return getOnlyElement(answer.get(varName).asThing().asRemote(transaction).attributes(transaction.getAttributeType(attributeType)).collect(Collectors.toList())).value();
    }

    public Object getValueOfAttribute(ConceptMap answer, String varName) {
        return answer.get(varName).asAttribute().value();
    }
//
//            @Override
//            public <T> List<T> getOrderedAttribute(Query query, String attributeName, Integer limit) {
//                return null;
//            }
}
