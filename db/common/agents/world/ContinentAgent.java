package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.World;

public abstract class ContinentAgent extends Agent<World.Continent> {

    protected World.Continent continent() {
        return item();
    }
}
