package grakn.simulation.db.neo4j.yaml_tool;

import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.common.yaml_tool.QueryTemplate;
import grakn.simulation.db.common.yaml_tool.YAMLLoader;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Neo4jYAMLLoader extends YAMLLoader {

    public Neo4jYAMLLoader(DriverWrapper.Session session, Map<String, Path> accessibleFiles) {
        super(session, accessibleFiles);
    }

    protected void parseCSV(DriverWrapper.Session.Transaction tx, QueryTemplate template, CSVParser parser) throws IOException {
        for (CSVRecord record : parser.getRecords()) {
            String interpolatedQuery = template.interpolate(record::get);
            tx.forNeo4j().run(interpolatedQuery);
        }
    }

}
