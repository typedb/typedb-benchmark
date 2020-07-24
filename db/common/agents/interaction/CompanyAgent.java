package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.world.CountryAgent;

public abstract class CompanyAgent extends CountryAgent {

    @Override
    public final void iterate() {

        int numCompanies = world().getScaleFactor();

        for (int i = 0; i < numCompanies; i++) {
            insertCompany(i);
        }
        tx().commit();
    }

    protected abstract void insertCompany(int i);
}