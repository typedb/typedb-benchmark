package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.World;

public abstract class CityAgent extends Agent<World.City> {
    protected World.City city() {
        return item();
    }
}
