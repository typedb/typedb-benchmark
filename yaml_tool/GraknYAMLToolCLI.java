package grakn.simulation.yaml_tool;

import grakn.client.GraknClient;
import grakn.client.GraknClient.Session;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

/**
 * A CLI to provide a useful tool for developers to test their YAML files.
 */
public class GraknYAMLToolCLI {

    @Option(name="-u", usage="Grakn server URI")
    private String graknUri = GraknClient.DEFAULT_URI;

    @Option(name="-k", usage="Grakn keyspace", required=true)
    private String graknKeyspace;

    @Option(name="-i", usage="Interactive mode, accept input from STDIN")
    private boolean interactiveMode;

    // The remaining arguments will be passed to this ArrayList and used as filenames to load.
    @Argument
    private List<File> files = new ArrayList<>();

    private void run() {
        GraknClient client = new GraknClient(graknUri);
        Session session = client.session(graknKeyspace);
        GraknYAMLLoader loader = new GraknYAMLLoader(session);

        for (File file : files) {
            try {
                loader.loadFile(file);
            } catch (FileNotFoundException | GraknYAMLException e) {
                System.err.println(e.getMessage());
            }
        }

        if (interactiveMode) {
            try {
                loader.loadInputStream(System.in);
            } catch (GraknYAMLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        GraknYAMLToolCLI loaderCLI = new GraknYAMLToolCLI();
        CmdLineParser parser = new CmdLineParser(loaderCLI);
        try {
            parser.parseArgument(args);
            loaderCLI.run();
        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: " + parser.printExample(ALL));
            System.exit(1);
        }
    }
}
