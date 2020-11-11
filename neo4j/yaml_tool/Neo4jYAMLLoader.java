package grakn.simulation.neo4j.yaml_tool;

import grakn.simulation.common.yaml_tool.QueryTemplate;
import grakn.simulation.common.yaml_tool.YAMLLoader;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Neo4jYAMLLoader extends YAMLLoader {

    private final Session session;

    public Neo4jYAMLLoader(Session session, Map<String, Path> accessibleFiles) {
        super(accessibleFiles);
        this.session = session;
    }

    @Override
    protected void parseCSV(QueryTemplate template, CSVParser parser) throws IOException {
        Transaction tx = session.beginTransaction();
        for (CSVRecord record : parser.getRecords()) {
            Query interpolatedQuery = new Query(template.interpolate(record::get));
            tx.run(interpolatedQuery);
        }
        tx.commit();
    }
}
