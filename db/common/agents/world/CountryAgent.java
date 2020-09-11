package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.world.World;

public abstract class CountryAgent<CONTEXT extends DatabaseContext> extends Agent<World.Country, CONTEXT> {

    protected World.Country country() {
        return region();
    }
}
