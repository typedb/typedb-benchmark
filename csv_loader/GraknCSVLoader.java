package grakn.simulation.csv_loader;

import grakn.client.GraknClient.Session;
import grakn.client.GraknClient.Transaction;
import graql.lang.query.GraqlInsert;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;

public class GraknCSVLoader {
    private Session session;

    public GraknCSVLoader(Session session) {
        this.session = session;
    }

    public void loadEntity(String entityType, CSVParser parser) throws IOException {
        Transaction transaction = session.transaction().write();

        GraknInsertFromCSV insertFromCSV = new GraknInsertFromCSV(entityType, parser.getHeaderNames());
        GraknInsertFromCSV.InsertQueryBuilder builder = insertFromCSV.builder();

        parser.getRecords().forEach(builder::add);

        GraqlInsert query = builder.build();

        transaction.execute(query);
        transaction.commit();
    }
}
