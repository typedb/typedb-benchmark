package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.World;

public abstract class CountryAgent<C> extends Agent<World.Country, C> {

    protected World.Country country() {
        return worldLocality();
    }
}
