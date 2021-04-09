/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.test;

import grakn.benchmark.config.AgentMode;
import grakn.benchmark.config.Config;
import grakn.benchmark.config.SamplingFunction;
import grakn.benchmark.grakn.GraknSimulation;
import grakn.benchmark.neo4j.Neo4JSimulation;
import grakn.benchmark.simulation.common.SimulationContext;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static grakn.benchmark.config.Config.Agent.ConstructAgentConfig;
import static grakn.benchmark.simulation.common.World.initialise;

public class BenchmarksForComparison {

    private static final Logger LOG = LoggerFactory.getLogger(BenchmarksForComparison.class);
    public static Neo4JSimulation neo4j;
    public static GraknSimulation graknCore;
    public static final int numIterations = 30;

    static {
        String[] args = System.getProperty("sun.java.command").split(" ");

        Options options = new Options();
        options.addOption(Option.builder("g")
                                  .longOpt("grakn-uri").desc("Grakn server URI").hasArg().required().argName("grakn-uri")
                                  .build());
        options.addOption(Option.builder("n")
                                  .longOpt("neo4j-uri").desc("Neo4j server URI").hasArg().required().argName("neo4j-uri")
                                  .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;

        String graknUri = null;
        String neo4jUri = null;
        try {
            commandLine = parser.parse(options, args);
            graknUri = commandLine.getOptionValue("g");
            neo4jUri = commandLine.getOptionValue("n");
        } catch (ParseException e) {
            LOG.error(e.getMessage(), e);
        }

        int scaleFactor = 5;
        int randomSeed = 1;

        Function<Integer, Boolean> samplingFunction = SamplingFunction.applyArg(SamplingFunction.getByName("every"), 1);

        Map<String, Path> files = new HashMap<>();

        List<String> dirPaths = new ArrayList<>();
        dirPaths.add("simulation/data");
        dirPaths.add("grakn/data");
        dirPaths.add("neo4j/data");

        dirPaths.forEach(dirPath -> {
            Arrays.asList(Objects.requireNonNull(Paths.get(dirPath).toFile().listFiles())).forEach(file -> {
                Path path = file.toPath();
                String filename = path.getFileName().toString();
                files.put(filename, path);
            });
        });

        ArrayList<String> agentNames = new ArrayList<>();
//        agentNames.add("marriage");
        agentNames.add("personBirth");
//        agentNames.add("ageUpdate");
//        agentNames.add("parentship");
//        agentNames.add("relocation");
        agentNames.add("company");
//        agentNames.add("employment");
        agentNames.add("product");
        agentNames.add("purchase");
//        agentNames.add("friendship");

        ArrayList<Config.Agent> agentConfigs = new ArrayList<>();
        agentNames.forEach(name -> agentConfigs.add(ConstructAgentConfig(name, AgentMode.RUN)));
        SimulationContext context = SimulationContext.create(initialise(scaleFactor, files), true);
        try {
            graknCore = GraknSimulation.core(graknUri, files, randomSeed, agentConfigs, context);
            neo4j = Neo4JSimulation.create(neo4jUri, files, randomSeed, agentConfigs, context);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
