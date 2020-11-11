package grakn.simulation.neo4j.driver;

import grakn.simulation.common.driver.LogWrapper;
import grakn.simulation.common.driver.TransactionalDbOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.EXECUTE;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.SORTED_EXECUTE;

public class Neo4jOperation extends TransactionalDbOperation {

    private final Session session;
    private final LogWrapper log;

    public Neo4jOperation(Session session, LogWrapper log, String tracker, boolean trace) {
        super(tracker, trace);
        this.session = session;
        this.log = log;
    }

    /**
     * Not necessary when using Neo4j's Transaction Functions
     */
    @Override
    public void close() {}

    /**
     * Not necessary when using Neo4j's Transaction Functions
     */
    @Override
    public void save() {}

    public List<Record> execute(Query query) {
        log.query(tracker, query);
        return trace(() -> session.writeTransaction( tx -> {
            Result result = tx.run(query);
            return result.list();
        }), EXECUTE.getName());
    }

    public <T> List<T> sortedExecute(Query query, String attributeName, Integer limit) {
        log.query(tracker, query);
        return trace(() -> {
            Stream<T> answerStream = execute(query).stream()
                    .map(record -> (T) record.asMap().get(attributeName))
                    .sorted();
            if (limit != null) {
                answerStream = answerStream.limit(limit);
            }
            return answerStream.collect(Collectors.toList());
        }, SORTED_EXECUTE.getName());
    }
}
