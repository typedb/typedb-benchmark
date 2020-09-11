package grakn.simulation.db.common.agents.region;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.world.World;

public abstract class ContinentAgent<CONTEXT extends DatabaseContext> extends Agent<World.Continent, CONTEXT> {

    protected World.Continent continent() {
        return region();
    }
}
