package grakn.simulation.test;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static grakn.simulation.test.SimulationsUnderTest.graknSimulation;
import static grakn.simulation.test.SimulationsUnderTest.neo4jSimulation;

public class SimulationTestSuite extends Suite {
    private static final List<Runner> NO_RUNNERS = Collections.emptyList();
    private final List<Runner> runners;
    private final Class<?> klass;
    private static int iteration = 0;

    public SimulationTestSuite(Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        this.klass = klass;
        this.runners = Collections.unmodifiableList(createRunnersForIterations());
    }

    private List<Runner> createRunnersForIterations() {
        List<Runner> runners = new ArrayList<>();
        for (int i = 0; i < SimulationsUnderTest.numIterations; i++) {
            try {
                BlockJUnit4ClassRunner runner = new SimulationRunner(klass, i);
                runners.add(runner);
            } catch (InitializationError initializationError) {
                initializationError.printStackTrace();
            }
        }
        return runners;
    }

    protected void runChild(Runner runner, final RunNotifier notifier) {
        iteration++;
        neo4jSimulation.iterate();
        graknSimulation.iterate();
        super.runChild(runner, notifier);
        if (iteration == SimulationsUnderTest.numIterations) {
            graknSimulation.close();
            neo4jSimulation.close();
        }
    }

    protected List<Runner> getChildren() {
        return this.runners;
    }
}
