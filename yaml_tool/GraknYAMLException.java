package grakn.simulation.yaml_tool;

public class GraknYAMLException extends Exception {
    public GraknYAMLException(String message) {
        super(generateMessage(message));
    }

    public GraknYAMLException(String message, Throwable cause) {
        super(generateMessage(message), cause);
    }

    private static String generateMessage(String message) {
        return "Grakn YAML error: " + message;
    }
}
