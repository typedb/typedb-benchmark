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

package grakn.benchmark;

import grabl.tracing.client.GrablTracing;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.benchmark.config.Config;
import grakn.benchmark.config.ConfigLoader;
import grakn.benchmark.common.world.World;
import grakn.benchmark.grakn.driver.GraknDriver;
import grakn.benchmark.neo4j.driver.Neo4jDriver;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static grabl.tracing.client.GrablTracing.tracing;
import static grabl.tracing.client.GrablTracing.tracingNoOp;
import static grabl.tracing.client.GrablTracing.withLogging;
import static grakn.benchmark.common.world.World.initialise;

public class BenchmarkRunner {

    final static Logger LOG = LoggerFactory.getLogger(grakn.benchmark.BenchmarkRunner.class);

    public static void main(String[] args) {

        ///////////////////
        // CONFIGURATION //
        ///////////////////

        Instant start = Instant.now();
        String defaultConfigYaml = args[0];
        Options options = cliOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        String dbName = getOption(commandLine, "d").orElse("grakn");
        String hostUri = getOption(commandLine, "s").orElse(null);
        String grablTracingUri = getOption(commandLine, "t").orElse("localhost:7979");
        String grablTracingOrganisation = commandLine.getOptionValue("o");
        String grablTracingRepository = commandLine.getOptionValue("r");
        String grablTracingCommit = commandLine.getOptionValue("c");
        String grablTracingUsername = commandLine.getOptionValue("u");
        String grablTracingToken = commandLine.getOptionValue("a");

        boolean disableTracing = commandLine.hasOption("n");

        Map<String, Path> initialisationDataFiles = new HashMap<>();
        for (String filepath : commandLine.getArgList()) {
            Path path = Paths.get(filepath);
            String filename = path.getFileName().toString();
            initialisationDataFiles.put(filename, path);
        }

        Path configPath = Paths.get(getOption(commandLine, "b").orElse(defaultConfigYaml));
        Config config = ConfigLoader.loadConfigFromYaml(configPath.toFile());

        ////////////////////
        // INITIALIZATION //
        ////////////////////

        // Components customised based on the DB
        String defaultUri;

        LOG.info("Welcome to the Benchmark!");
        LOG.info("Parsing world data...");
        World world = initialise(config.getScaleFactor(), initialisationDataFiles);
        if (world == null) return;

        LOG.info(String.format("Connecting to %s...", dbName));

        try {
            try (GrablTracing tracingIgnored = grablTracing(grablTracingUri, grablTracingOrganisation, grablTracingRepository, grablTracingCommit, grablTracingUsername, grablTracingToken, disableTracing, dbName)) {
                grakn.benchmark.common.Benchmark<?, ?> benchmark;
                if (dbName.toLowerCase().startsWith("grakn")) {
                    defaultUri = "localhost:48555";
                    if (hostUri == null) hostUri = defaultUri;

                    benchmark = new grakn.benchmark.grakn.GraknBenchmark(
                            new GraknDriver(hostUri, "world"),
                            initialisationDataFiles,
                            config.getRandomSeed(),
                            world,
                            config.getAgents(),
                            config.getTraceSampling().getSamplingFunction(),
                            false);
                } else if (dbName.toLowerCase().startsWith("neo4j")) {
                    defaultUri = "bolt://localhost:7687";
                    if (hostUri == null) hostUri = defaultUri;

                    benchmark = new grakn.benchmark.neo4j.Neo4JBenchmark(
                            new Neo4jDriver(hostUri),
                            initialisationDataFiles,
                            config.getRandomSeed(),
                            world,
                            config.getAgents(),
                            config.getTraceSampling().getSamplingFunction(),
                            false);
                } else {
                    throw new IllegalArgumentException("Unexpected value: " + dbName);
                }

                ///////////////
                // MAIN LOOP //
                ///////////////
                for (int i = 0; i < config.getIterations(); i++) {
                    benchmark.iterate();
                }
                benchmark.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        Instant end = Instant.now();
        LOG.info("Benchmark completed in " + Duration.between(start, end).toString().substring(2));
    }

    private static Optional<String> getOption(CommandLine commandLine, String option) {
        if (commandLine.hasOption(option)) {
            return Optional.of(commandLine.getOptionValue(option));
        } else {
            return Optional.empty();
        }
    }

    private static Options cliOptions() {
        Options options = new Options();
        options.addOption(Option.builder("d")
                .longOpt("database").desc("Database under test").hasArg().required().argName("database")
                .build());
        options.addOption(Option.builder("s")
                .longOpt("database-uri").desc("Database server URI").hasArg().argName("uri")
                .build());
        options.addOption(Option.builder("t")
                .longOpt("tracing-uri").desc("Grabl tracing server URI").hasArg().argName("uri")
                .build());
        options.addOption(Option.builder("o")
                .longOpt("org").desc("Repository organisation").hasArg().argName("name")
                .build());
        options.addOption(Option.builder("r")
                .longOpt("repo").desc("Grabl tracing repository").hasArg().argName("name")
                .build());
        options.addOption(Option.builder("c")
                .longOpt("commit").desc("Grabl tracing commit").hasArg().argName("sha")
                .build());
        options.addOption(Option.builder("u")
                .longOpt("username").desc("Grabl tracing username").hasArg().argName("username")
                .build());
        options.addOption(Option.builder("a")
                .longOpt("api-token").desc("Grabl tracing API token").hasArg().argName("token")
                .build());
        options.addOption(Option.builder("b")
                .longOpt("config-file").desc("Configuration file").hasArg().argName("config-file-path")
                .build());
        options.addOption(Option.builder("n")
                .longOpt("disable-tracing").desc("Disable grabl tracing")
                .build());
        return options;
    }

    public static GrablTracing grablTracing(String grablTracingUri, String grablTracingOrganisation, String grablTracingRepository, String grablTracingCommit, String grablTracingUsername, String grablTracingToken, boolean disableTracing, String name) {
        GrablTracing tracing;
        if (disableTracing) {
            tracing = withLogging(tracingNoOp());
        } else if (grablTracingUsername == null) {
            tracing = withLogging(tracing(grablTracingUri));
        } else {
            tracing = withLogging(tracing(grablTracingUri, grablTracingUsername, grablTracingToken));
        }
        GrablTracingThreadStatic.setGlobalTracingClient(tracing);
        GrablTracingThreadStatic.openGlobalAnalysis(grablTracingOrganisation, grablTracingRepository, grablTracingCommit, name);
        return tracing;
    }
}
