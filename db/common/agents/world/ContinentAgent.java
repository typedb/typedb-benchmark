package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.World;

public abstract class ContinentAgent<C> extends Agent<World.Continent, C> {

    protected World.Continent continent() {
        return worldLocality();
    }
}
