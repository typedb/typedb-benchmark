package grakn.simulation.common.yaml_tool;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

/**
 * A class to automate loading data into Grakn simply using YAML files with embedded Graql and CSV.
 *
 * Multi-doc YAML is supported.
 *
 * The YAML document should contain a "template" and a "data" parameter. The "template" should be a Graql query using
 * angle brackets with numbers to indicate the column index (beginning with 0) of the variable in the CSV "data"
 * section.
 */
public abstract class YAMLLoader {

    // Instances of YAML should not be shared across threads but are okay to be re-used within a thread.
    protected static final ThreadLocal<Yaml> THREAD_YAML = ThreadLocal.withInitial(Yaml::new);
    private final Map<String, Path> accessibleFiles;

    public YAMLLoader(Map<String, Path> accessibleFiles) {
        this.accessibleFiles = accessibleFiles;
    }

    public void loadFile(File loadFile) throws YAMLException, FileNotFoundException {
        loadInputStream(new FileInputStream(loadFile));
    }

    public void loadInputStream(InputStream inputStream) throws YAMLException {
        for (Object document : THREAD_YAML.get().loadAll(inputStream)) {
            loadDocument(document);
        }
    }

    protected void loadDocument(Object document) throws YAMLException {
        Map documentMap;
        try {
            documentMap = (Map) document;
        } catch (ClassCastException e) {
            throw new YAMLException("Document was not a Map.");
        }

        // Get template
        String templateString = getString(documentMap, "template");
        if (templateString == null) {
            throw new YAMLException("No template was supplied.");
        }
        QueryTemplate template = new QueryTemplate(templateString);

        // Try data_file
        String dataFile = getString(documentMap, "data_file");
        if (dataFile != null) {
            try {
                CSVParser parser = CSVParser.parse(accessibleFiles.get(dataFile), StandardCharsets.UTF_8, CSVFormat.DEFAULT);
                parseCSV(template, parser);
            } catch (IOException e) {
                throw new YAMLException("Could not parse CSV data.", e);
            }
        }
    }

    protected abstract void parseCSV(QueryTemplate template, CSVParser parser) throws IOException;

    /**
     * Helper method to avoid repeating nasty type checking code.
     *
     * @param object The document object Map.
     * @param key A String key to lookup in the Map.
     * @return The String found.
     * @throws YAMLException when something other than a String is found.
     */
    private static String getString(Map object, String key) throws YAMLException {
        Object result = object.get(key);
        if (result == null) {
            return null;
        }
        if (!(result instanceof String)) {
            throw new YAMLException("Did not find a String for '" + key + "'");
        }
        return (String) result;
    }
}
