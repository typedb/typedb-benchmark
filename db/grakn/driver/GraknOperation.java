package grakn.simulation.db.grakn.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.driver.LogWrapper;
import grakn.simulation.db.common.driver.TransactionalDbOperation;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.EXECUTE;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.SORTED_EXECUTE;

public class GraknOperation extends TransactionalDbOperation {

    private final GraknClient.Transaction transaction;
    private final LogWrapper log;

    boolean closed = false;

    public GraknOperation(GraknClient.Session session, LogWrapper log, String tracker, boolean trace) {
        super(tracker, trace);
        this.transaction = session.transaction(GraknClient.Transaction.Type.WRITE);
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

    public <T> List<T> sortedExecute(GraqlGet query, String attributeName, Integer limit){
        throwIfClosed();
        log.query(tracker, query);
        return trace(() -> {
            Stream<T> answerStream = transaction.stream(query).get()
                    .map(conceptMap -> (T) conceptMap.get(attributeName).asAttribute().value())
                    .sorted();
            if (limit != null) {
                answerStream = answerStream.limit(limit);
            }
            return answerStream.collect(Collectors.toList());
        }, SORTED_EXECUTE.getName());
    }

    public int count(GraqlGet.Aggregate countQuery) {
        throwIfClosed();
        log.query(tracker, countQuery);
        return getOnlyElement(transaction.execute(countQuery).get()).number().intValue();
    }

    public void execute(GraqlDelete query) {
        log.query(tracker, query);
        trace(() -> transaction.execute(query).get(), EXECUTE.getName());
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

    public Number execute(GraqlGet.Aggregate query) {
        log.query(tracker, query);
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(EXECUTE.getName())) {
            return getOnlyElement(transaction.execute(query).get()).number();
        }
    }

    public Object getOnlyAttributeOfThing(ConceptMap answer, String varName, String attributeType) {
        return getOnlyElement(answer.get(varName).asThing().asRemote(transaction).attributes(transaction.getAttributeType(attributeType)).collect(Collectors.toList())).value();
    }

    public Object getValueOfAttribute(ConceptMap answer, String varName) {
        return answer.get(varName).asAttribute().value();
    }
}
