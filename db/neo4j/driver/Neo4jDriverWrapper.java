package grakn.simulation.db.neo4j.driver;

import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.neo4j.agents.interaction.Neo4jQuery;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.AsyncTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Neo4jDriverWrapper implements DriverWrapper {

    private Driver driver = null;

    public Driver getClient() {
        return driver;
    }

    @Override
    public Neo4jDriverWrapper open(String uri) {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( "neo4j", "admin" ) );;
        return this;
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public Session session(String database) {
        return new Session(driver.asyncSession());
    }

    public static class Session extends DriverWrapper.Session {

        private AsyncSession session;
        private boolean transactionClosed = false;
        private Transaction transaction;

        Session(AsyncSession session) {
            this.session = session;
            transaction = new Transaction(session);
        }

        @Override
        public void close() {
            session.closeAsync();
        }

        @Override
        public Transaction transaction() {
            if (transactionClosed) {
                transaction = new Transaction(session);
            }
            return transaction;
        }

        public class Transaction extends DriverWrapper.Session.Transaction {

            private CompletionStage<AsyncTransaction> transaction;
            private List<Neo4jQuery> queries = new ArrayList<>();
            private AsyncSession session;

            Transaction(AsyncSession session) {
                this.session = session;
                this.transaction = newTransaction();
            }

            private CompletionStage<AsyncTransaction> newTransaction() {
                return session.beginTransactionAsync();
            }

            @Override
            public void close() {
//                transaction.close();
//                throw new RuntimeException();
//                System.out.println("Transaction closed");
//                transaction.thenCompose(AsyncTransaction::commitAsync);
//                transaction.thenCompose(ignore -> session.closeAsync());
//                if (!transactionClosed) {
////                    try {
////                        transaction.thenCompose(AsyncTransaction::rollbackAsync).toCompletableFuture().get();
//                        transaction.thenCompose(AsyncTransaction::rollbackAsync);
////                    } catch (InterruptedException | ExecutionException e) {
////                        e.printStackTrace();
////                    }
//                }
            }

            @Override
            public void commit() {
                try {
                    transaction.thenCompose(AsyncTransaction::commitAsync).toCompletableFuture().get();
                    transactionClosed = true;
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
//                Throwable txEx = null;
//                int RETRIES = 5;
//                int BACKOFF = 100;
//                for (int i = 0; i < RETRIES; i++) {
//                    try {
//                        transaction.commit();
//                        return;
//                    } catch (Throwable ex) {
//                        txEx = ex;
//
//                        // Add whatever exceptions to retry on here
//                        if (!(ex instanceof TransientException)) {
//                            throw ex;
//                        }
//                    }
//
//                    // Wait so that we don't immediately get into the same deadlock
//                    if (i < RETRIES - 1) {
//                        try {
//                            transaction = newTransaction();
//                            replay();
//                            Thread.sleep(BACKOFF);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException("Interrupted", e);
//                        }
//                    } else {
//                        throw new RuntimeException("Exceeded the number of retries", txEx);
//                    }
//                }
//                throw ((RuntimeException) txEx);
            }

            void addQuery(Neo4jQuery query) {
                this.queries.add(query);
            }

            private void replay() {
//                queries.forEach(q -> transaction.run(q.template(), parameters(q.parameters())));
            }

            @Override
            public CompletionStage<AsyncTransaction> forNeo4j() {
                return transaction;
            }

            public AsyncSession getSession() {
                return session;
            }

            public void run(Query query) {

                CompletionStage<AsyncTransaction> future = transaction.thenCompose(tx ->
                                tx.runAsync(query)
                                .exceptionally(e -> {
                                    e.printStackTrace();
                                    return null;
                                })
                                .thenApply(ignore -> tx)
                );
                try {
                    future.toCompletableFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            public <T> List<T> getOrderedAttribute(Query query, String attributeName, Integer limit){
                //TODO Use limit if not null
                CompletionStage<List<T>> objectCompletionStage = transaction.thenCompose(tx ->
                        tx.runAsync(query)
                                .exceptionally(e -> {
                                    e.printStackTrace();
                                    return null;
                                })
                                .thenCompose(resultCursor -> resultCursor.listAsync(record -> (T) record.asMap().get(attributeName)))
                );

//                CompletionStage<ResultSummary> objectCompletionStage = transaction.thenCompose(tx ->
//                        tx.runAsync(query).thenCompose(ResultCursor::consumeAsync)
//                );
//                return objectCompletionStage.toCompletableFuture().join();


                try {
                    return objectCompletionStage.toCompletableFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }

//                return run(tx, neo4jQuery).stream()
//                        .map(record -> (T) record.asMap().get(attributeName))
//                        .sorted()
//                        .limit(limit)
//                        .collect(Collectors.toList());
            }
        }
    }

//    public static Result run(DriverWrapper.Session.Transaction transaction, Neo4jQuery query) {
//
////        String query = "MATCH (p:Product) WHERE p.id = $id RETURN p.title";
////        Map<String,Object> parameters = Collections.singletonMap( "id", 0 );
//
//
//        AsyncSession session = ((Session.Transaction) transaction).getSession();
////        transaction.forNeo4j().run
//
//
//        session.beginTransactionAsync()
//                .thenCompose(tx ->
//                        tx.runAsync("CREATE (a:Person {name: $name})", parameters("name", "Alice"))
//                                .exceptionally(e -> {
//                                    e.printStackTrace();
//                                    return null;
//                                })
//                                .thenApply(ignore -> tx)
//                ).thenCompose(AsyncTransaction::commitAsync);
//
//
////        AsyncSession session = driver.asyncSession();
//
//
////        CompletionStage<List<String>> objectCompletionStage = session.readTransactionAsync(tx ->
////                tx.runAsync(query.template(), query.parameters())
////                        .thenCompose(cursor -> cursor.listAsync( record ->
////                                record.get( 0 ).asString())));
////        return objectCompletionStage;
////
////        CompletionStage<List<String>> objectCompletionStage = session.writeTransactionAsync(tx ->
////                tx.runAsync(query.template(), query.parameters())
////                        .thenCompose(cursor -> cursor.listAsync( record ->
////                                record.get( 0 ).asString())));
////        return objectCompletionStage;
//
//
//        ((Session.Transaction) transaction).addQuery(query);
//        return transaction.forNeo4j().run(query.template(), parameters(query.parameters()));
//    }
}
