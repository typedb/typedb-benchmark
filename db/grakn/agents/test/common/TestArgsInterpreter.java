package agents.test.common;

public class TestArgsInterpreter {

    private String host;
    private static final String[] args = System.getProperty("sun.java.command").split(" ");

    public TestArgsInterpreter() {
        if (args.length == 1) {
            host = "localhost:48555";
        } else if (args.length == 2) {
            host = args[1];
        } else {
            throw new IllegalArgumentException("Received more arguments than expected. Accepts one argument, `grakn-uri`, or no arguments to use the default Grakn host.");
        }
    }

    public String getHost() {
        return host;
    }
}
