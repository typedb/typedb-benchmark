package grakn.simulation.yaml_tool;

import grakn.client.GraknClient.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.yaml.snakeyaml.Yaml;
import grakn.client.GraknClient.Session;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * A class to automate loading data into Grakn simply using YAML files with embedded Graql and CSV.
 *
 * Multi-doc YAML is supported.
 *
 * The YAML document should contain a "template" and a "data" parameter. The "template" should be a Graql query using
 * angle brackets with numbers to indicate the column index (beginning with 0) of the variable in the CSV "data"
 * section.
 */
public class GraknYAMLLoader {

    // Instances of YAML should not be shared across threads but are okay to be re-used within a thread.
    private static final ThreadLocal<Yaml> THREAD_YAML = ThreadLocal.withInitial(Yaml::new);
    private Session session;

    public GraknYAMLLoader(Session session) {
        this.session = session;
    }

    public void loadFile(File file) throws GraknYAMLException, FileNotFoundException {
        loadInputStream(new FileInputStream(file), file.toPath().getParent());
    }

    public void loadInputStream(InputStream inputStream, Path workingDir) throws GraknYAMLException {
        try (Transaction tx = session.transaction().write()) {
            for (Object document : THREAD_YAML.get().loadAll(inputStream)) {
                loadDocument(tx, document, workingDir);
            }

            tx.commit();
        }
    }

    private void loadDocument(Transaction tx, Object document, Path workingDir) throws GraknYAMLException {
        Map documentMap;
        try {
            documentMap = (Map) document;
        } catch (ClassCastException e) {
            throw new GraknYAMLException("Document was not a Map.");
        }

        // Get template
        String templateString = getString(documentMap, "template");
        if (templateString == null) {
            throw new GraknYAMLException("No template was supplied.");
        }
        GraqlQueryTemplate template = new GraqlQueryTemplate(templateString);

        // Try data_file
        String dataFile = getString(documentMap, "data_file");
        if (dataFile != null) {
            try {
                CSVParser parser = CSVParser.parse(workingDir.resolve(dataFile), StandardCharsets.UTF_8, CSVFormat.DEFAULT);
                parseCSV(tx, template, parser);
            } catch (IOException e) {
                throw new GraknYAMLException("Could not parse CSV data.", e);
            }
        }

        // Try data attrib
        String data = getString(documentMap, "data");
        if (data != null) {
            try {
                CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT);
                parseCSV(tx, template, parser);
            } catch (IOException e) {
                throw new GraknYAMLException("Could not parse CSV data.", e);
            }
        }
    }

    private void parseCSV(Transaction tx, GraqlQueryTemplate template, CSVParser parser) throws IOException {
        for (CSVRecord record : parser.getRecords()) {
            String interpolatedQuery = template.interpolate(record::get);
            GraqlInsert insert = Graql.parse(interpolatedQuery);
            tx.execute(insert);
        }
    }

    /**
     * Helper method to avoid repeating nasty type checking code.
     *
     * @param object The document object Map.
     * @param key A String key to lookup in the Map.
     * @return The String found.
     * @throws GraknYAMLException when something other than a String is found.
     */
    private static String getString(Map object, String key) throws GraknYAMLException {
        Object result = object.get(key);
        if (result == null) {
            return null;
        }
        if (!(result instanceof String)) {
            throw new GraknYAMLException("Did not find a String for '" + key + "'");
        }
        return (String) result;
    }
}
