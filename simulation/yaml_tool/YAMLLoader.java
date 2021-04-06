/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.simulation.yaml_tool;

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
     * @param key    A String key to lookup in the Map.
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
