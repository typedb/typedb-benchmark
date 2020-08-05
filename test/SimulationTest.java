package grakn.simulation.test;

import grakn.simulation.Simulation;
import grakn.simulation.config.Schema;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.base.ResultHandler;
import grakn.simulation.db.common.agents.interaction.PersonBirthAgentBase;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknClientWrapper;
import grakn.simulation.db.grakn.initialise.GraknAgentPicker;
import grakn.simulation.db.grakn.initialise.GraknInitialiser;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import grakn.simulation.db.neo4j.initialise.Neo4jAgentPicker;
import grakn.simulation.db.neo4j.initialise.Neo4jInitialiser;
import grakn.simulation.utils.RandomSource;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.InitializationError;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import org.junit.runners.parameterized.TestWithParameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static grakn.simulation.db.common.initialise.Initialiser.world;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(SimulationTest.RunnerFactory.class)
public class SimulationTest {

    private static final Simulation neo4jSimulation;
    private static final Simulation graknSimulation;
    private static final int numIterations = 3;
    private static int iteration;

    static {
        int scaleFactor = 5;
        int randomSeed = 1;

        Function<Integer, Boolean> samplingFunction = Schema.SamplingFunction.applyArg(Schema.SamplingFunction.getByName("every"), 1);

        Map<String, Path> files = new HashMap<>();

        List<String> dirPaths = new ArrayList<>();
        dirPaths.add("db/grakn/schema");
        dirPaths.add("db/common/data");
        dirPaths.add("db/grakn/schema");
        dirPaths.add("db/grakn/data");
        dirPaths.add("db/neo4j/data");


        dirPaths.forEach(dirPath -> {
            Arrays.asList(Objects.requireNonNull(Paths.get(dirPath).toFile().listFiles())).forEach(file -> {
                Path path = file.toPath();
                String filename = path.getFileName().toString();
                files.put(filename, path);
            });
        });

        ArrayList<String> agentNames = new ArrayList<>();
        agentNames.add("marriage");
        agentNames.add("personBirth");
        agentNames.add("ageUpdate");

        World world = world(scaleFactor, files);

        /////////////////
        // Grakn setup //
        /////////////////

        String graknUri = "localhost:48555";
        GraknAgentPicker graknAgentPicker = new GraknAgentPicker();
        GraknInitialiser graknInitialiser = new GraknInitialiser(files);
        GraknClientWrapper graknDriverWrapper = new GraknClientWrapper();
        graknDriverWrapper.open(graknUri);

        List<AgentRunner<?>> graknAgentRunners = getAgentRunners(graknAgentPicker, agentNames);
        ResultHandler graknResultHandler = new ResultHandler();

        graknSimulation = new Simulation(
                graknDriverWrapper,
                "world",
                graknInitialiser,
                graknAgentRunners,
                new RandomSource(randomSeed),
                world,
                samplingFunction,
                graknResultHandler
        );

        /////////////////
        // Neo4j setup //
        /////////////////

        String neo4jUri = "bolt://localhost:7687";
        Neo4jAgentPicker neo4jAgentPicker = new Neo4jAgentPicker();
        Neo4jInitialiser neo4jInitialiser = new Neo4jInitialiser(files);
        Neo4jDriverWrapper neo4jDriverWrapper = new Neo4jDriverWrapper();
        neo4jDriverWrapper.open(neo4jUri);

        List<AgentRunner<?>> neo4jAgentRunners = getAgentRunners(neo4jAgentPicker, agentNames);
        ResultHandler neo4jResultHandler = new ResultHandler();

        neo4jSimulation = new Simulation(
                neo4jDriverWrapper,
                "",
                neo4jInitialiser,
                neo4jAgentRunners,
                new RandomSource(randomSeed),
                world,
                samplingFunction,
                neo4jResultHandler
        );
    }

    @Parameterized.Parameters
    public static Iterable<Integer> data() {

        // Build test parameters
        ArrayList<Integer> data = new ArrayList<Integer>() {};
        for (int i = 1; i <= numIterations; i++) {
            data.add(i);
        }
        return data;
    }

    private static List<AgentRunner<?>> getAgentRunners(AgentPicker agentPicker, List<String> agentNames) {
        List<AgentRunner<?>> agentRunners = new ArrayList<>();
        agentNames.forEach(agentName -> {
            AgentRunner<?> runner = agentPicker.get(agentName);
            agentRunners.add(runner);
        });
        return agentRunners;
    }

    public SimulationTest(Integer it) {
        iteration = it;
        neo4jSimulation.iterate();
        graknSimulation.iterate();
    }

    @Test
    public void testPersonBirthAgent() {
        Collection<HashMap<PersonBirthAgentBase.Field, Object>> graknFields = graknSimulation.getResultHandler().getResultForAgent("PersonBirthAgent").getAllFieldValues();
        Collection<HashMap<PersonBirthAgentBase.Field, Object>> neo4jFields = neo4jSimulation.getResultHandler().getResultForAgent("PersonBirthAgent").getAllFieldValues();
        assertEquals(graknFields, neo4jFields);
    }

    @Test
    public void testAgeUpdateAgent() {

    }

    @AfterClass
    public static void afterClass() {
        if (iteration == numIterations) {
            graknSimulation.close();
            neo4jSimulation.close();
        }
    }

    public static class RunnerFactory implements ParametersRunnerFactory {
        @Override
        public org.junit.runner.Runner createRunnerForTestWithParameters(TestWithParameters test) throws InitializationError {
            return new SimTest(test);
        }
    }

    public static class SimTest extends BlockJUnit4ClassRunnerWithParameters {
        private final Object[] parameters;

        public SimTest(TestWithParameters test) throws InitializationError {
            super(test);
            parameters = test.getParameters().toArray(new Object[test.getParameters().size()]);
        }

        @Override
        public Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(parameters);
        }
    }
}
