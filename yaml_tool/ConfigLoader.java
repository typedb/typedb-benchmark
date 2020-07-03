package grakn.simulation.yaml_tool;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;

public class ConfigLoader {

    public static Config loadConfigFromYaml(File file) throws YAMLException {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        try {
            InputStream inputStream = new FileInputStream(new File(file.toPath().toString()));
            return yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't find config file");
        }
    }
}
