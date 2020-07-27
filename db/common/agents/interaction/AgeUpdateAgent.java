package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.world.CityAgent;

public abstract class AgeUpdateAgent extends CityAgent {

    @Override
    public final void iterate() {
        updateAgesOfAllPeople();
        tx().commitWithTracing();
    }

    protected abstract void updateAgesOfAllPeople();
}
