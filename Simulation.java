package grakn.simulation;

import grakn.simulation.world.World;
import grakn.simulation.agents.base.IterationContext;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.driver.DbDriverWrapper;
import grakn.simulation.common.RandomSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static grabl.tracing.client.GrablTracing.tracing;

public class Simulation implements IterationContext, AutoCloseable {

    private final DbDriverWrapper dbDriver;
    private final String keyspace;
    private final DbDriverWrapper.Session defaultSession;
    private final List<AgentRunner> agentRunners;
    private final Random random;
    private Function<Integer, Boolean> iterationSamplingFunction;
    private final World world;

    private int simulationStep = 1;

    private final ConcurrentMap<String, DbDriverWrapper.Session> sessionMap;

    Simulation(DbDriverWrapper dbDriver, String keyspace, List<AgentRunner> agentRunners, RandomSource randomSource, World world, Function<Integer, Boolean> iterationSamplingFunction) {
        this.dbDriver = dbDriver;
        this.keyspace = keyspace;
        defaultSession = this.dbDriver.session(keyspace);
        this.agentRunners = agentRunners;
        random = randomSource.startNewRandom();
        this.iterationSamplingFunction = iterationSamplingFunction;
        sessionMap = new ConcurrentHashMap<>();
        this.world = world;
    }

    void iterate() {

        RunSimulation.LOG.info("Simulation step: {}", simulationStep);

        for (AgentRunner agentRunner : agentRunners) {
            agentRunner.iterate(this, RandomSource.nextSource(random));
        }

        closeAllSessionsInMap(); // We want to test opening new sessions each iteration.

        simulationStep++;
    }

    private void closeAllSessionsInMap() {
        for (DbDriverWrapper.Session session : sessionMap.values()) {
            session.close();
        }
        sessionMap.clear();
    }
    
    @Override
    public DbDriverWrapper.Session getIterationSessionFor(String key) {
        return sessionMap.computeIfAbsent(key, k -> dbDriver.session(keyspace)); // Open sessions for new keys
    }

    @Override
    public int getSimulationStep() {
        return simulationStep;
    }

    @Override
    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(LocalDate.ofYearDay(simulationStep, 1), LocalTime.of(0, 0, 0));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public boolean shouldTrace() {
        return iterationSamplingFunction.apply(getSimulationStep());
    }

    @Override
    public void close() {
        closeAllSessionsInMap();

        defaultSession.close();

        if (dbDriver != null) {
            dbDriver.close();
        }
    }
}
