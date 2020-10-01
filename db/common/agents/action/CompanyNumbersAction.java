package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.world.World;

import java.util.List;

public abstract class CompanyNumbersAction extends Action<List<Long>> {

    protected final World.Country country;
    protected final int numCompanies;

    public CompanyNumbersAction(World.Country country, int numCompanies) {
        super(dbOperation);
        this.country = country;
        this.numCompanies = numCompanies;
    }

    @Override
    public Report resultsForTesting(List<Long> companyNumbers) {
        return new Report(){{
            put("COMPANY_NUMBERS", companyNumbers);
        }};
    }
}
