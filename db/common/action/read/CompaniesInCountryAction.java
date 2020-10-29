package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class CompaniesInCountryAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, List<Long>> {

    protected final World.Country country;
    protected final int numCompanies;

    public CompaniesInCountryAction(DB_OPERATION dbOperation, World.Country country, int numCompanies) {
        super(dbOperation);
        this.country = country;
        this.numCompanies = numCompanies;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(country, numCompanies);
    }
}
