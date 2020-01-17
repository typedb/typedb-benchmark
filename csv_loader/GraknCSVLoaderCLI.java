package grakn.simulation.csv_loader;

import grakn.client.GraknClient;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class GraknCSVLoaderCLI {

    @Option(name="-f", usage="CSV Filename")
    private File csvFile;

    @Option(name="-u", usage="Grakn server URI")
    private String graknUri = GraknClient.DEFAULT_URI;

    @Option(name="-k", usage="Grakn keyspace")
    private String graknKeyspace;

    // TODO add CSVFormat option

    @Argument
    private List<String> arguments = new ArrayList<>();

    private GraknClient.Session getSesssion() {
        GraknClient client = new GraknClient(graknUri);
        return client.session(graknKeyspace);
    }

    private void run() {
        try {
            GraknCSVLoader loader = new GraknCSVLoader(getSesssion());

            if (csvFile == null) {
                if (arguments.size() != 1) {
                    System.out.println("Please specify an entity name as a single argument.");
                    System.exit(1);
                }

                loader.loadEntity(
                        arguments.get(0),
                        CSVParser.parse(System.in, Charset.defaultCharset(), CSVFormat.DEFAULT.withFirstRecordAsHeader())
                );
            } else {
                String entityName = stripExtension(csvFile.getName());
                loader.loadEntity(
                        entityName,
                        CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withFirstRecordAsHeader())
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String stripExtension(String filename) {
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    public static void main(String[] args) {
        GraknCSVLoaderCLI loaderCLI = new GraknCSVLoaderCLI();
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
