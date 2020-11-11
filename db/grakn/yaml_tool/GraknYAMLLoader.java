package grakn.simulation.db.grakn.yaml_tool;

import grakn.client.GraknClient;
import grakn.simulation.common.yaml_tool.QueryTemplate;
import grakn.simulation.common.yaml_tool.YAMLLoader;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class GraknYAMLLoader extends YAMLLoader {

    private final GraknClient.Session session;

    public GraknYAMLLoader(GraknClient.Session session, Map<String, Path> accessibleFiles) {
        super(accessibleFiles);
        this.session = session;
    }

    @Override
    protected void parseCSV(QueryTemplate template, CSVParser parser) throws IOException {
        try (GraknClient.Transaction tx = session.transaction(GraknClient.Transaction.Type.WRITE)) {
            for (CSVRecord record : parser.getRecords()) {
                String interpolatedQuery = template.interpolate(record::get);
                GraqlInsert insert = Graql.parse(interpolatedQuery);
                tx.execute(insert);
            }
            tx.commit();
        }
    }
}
