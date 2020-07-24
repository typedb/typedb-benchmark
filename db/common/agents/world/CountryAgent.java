package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.World;

public abstract class CountryAgent extends Agent<World.Country> {

    protected World.Country country() {
        return item();
    }
}
