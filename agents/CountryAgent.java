package grakn.simulation.agents;

public abstract class CountryAgent extends Agent<World.Country> {

    protected World.Country country() {
        return item();
    }
}
