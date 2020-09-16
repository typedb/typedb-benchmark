package grakn.simulation.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class SimulationRunner extends BlockJUnit4ClassRunner {
    private final int iteration;

    public SimulationRunner(Class<?> aClass, int iteration) throws InitializationError {
        super(aClass);
        this.iteration = iteration;
    }

    @Override
    protected String testName(FrameworkMethod method) {
        return method.getName() + "-it" + iteration;
    }

    @Override
    protected String getName() {
        return super.getName() + "-iteration" + iteration;
    }
}
