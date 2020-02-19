package grakn.simulation.agents;

public abstract class CityAgent extends Agent<World.City> {
    protected World.City city() {
        return item();
    }
}
