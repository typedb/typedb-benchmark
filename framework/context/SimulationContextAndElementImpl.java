package grakn.simulation.framework.context;

class SimulationContextAndElementImpl<T> implements SimulationContextAndElement<T> {
    private SimulationContext context;
    private T item;

    SimulationContextAndElementImpl(SimulationContext context, T item) {
        this.context = context;
        this.item = item;
    }

    @Override
    public SimulationContext getContext() {
        return context;
    }

    @Override
    public T getItem() {
        return item;
    }
}
