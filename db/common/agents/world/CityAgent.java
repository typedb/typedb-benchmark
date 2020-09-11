package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.world.World;

public abstract class CityAgent<CONTEXT extends DatabaseContext> extends Agent<World.City, CONTEXT> {
    protected World.City city() {
        return region();
    }
}
