package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.util.HashMap;
import java.util.List;

public abstract class CompanyNumbersAction<DB_OPERATION extends DbOperationController.DbOperation> extends Action<DB_OPERATION, List<Long>> {

    protected final World.Country country;
    protected final int numCompanies;

    public CompanyNumbersAction(DB_OPERATION dbOperation, World.Country country, int numCompanies) {
        super(dbOperation);
        this.country = country;
        this.numCompanies = numCompanies;
    }

    @Override
    public HashMap<String, Object> outputForReport(List<Long> companyNumbers) {
        return new HashMap<String, Object>(){{
            put("COMPANY_NUMBERS", companyNumbers);
        }};
    }
}
