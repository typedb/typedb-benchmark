package grakn.simulation.framework.context;

public interface SimulationContextAndElement<T> {
    SimulationContext getContext();
    T getItem();
}
