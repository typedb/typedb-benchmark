package grakn.simulation.grakn.yaml_tool;

import grakn.simulation.driver.DriverWrapper;
import grakn.simulation.yaml_tool.QueryTemplate;
import grakn.simulation.yaml_tool.YAMLLoader;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;

public class GraknYAMLLoader extends YAMLLoader {
    public GraknYAMLLoader(DriverWrapper.Session session) {
        super(session);
    }

    protected void parseCSV(DriverWrapper.Session.Transaction tx, QueryTemplate template, CSVParser parser) throws IOException {
        for (CSVRecord record : parser.getRecords()) {
            String interpolatedQuery = template.interpolate(record::get);
            GraqlInsert insert = Graql.parse(interpolatedQuery);
            tx.forGrakn().execute(insert);
        }
    }

}
