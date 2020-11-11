package grakn.simulation.common.yaml_tool;

public class YAMLException extends Exception {
    public YAMLException(String message) {
        super(generateMessage(message));
    }

    public YAMLException(String message, Throwable cause) {
        super(generateMessage(message), cause);
    }

    private static String generateMessage(String message) {
        return "Grakn YAML error: " + message;
    }
}
