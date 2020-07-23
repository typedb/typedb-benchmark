package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.world.CityAgent;
import grakn.simulation.db.common.driver.DriverWrapper;

public abstract class AgeUpdateAgent<S extends DriverWrapper.Session<S, T>, T extends DriverWrapper.Transaction> extends CityAgent<S, T> {

    @Override
    public final void iterate() {
        updateAgesOfAllPeople();
        tx().commit();
    }

    protected abstract void updateAgesOfAllPeople();
}
