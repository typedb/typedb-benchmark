package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public abstract class ResidentsInCityAction<DB_OPERATION extends DbOperationController.DbOperation> extends Action<DB_OPERATION, List<String>> {

    protected final World.City city;
    protected final int numResidents;
    protected final LocalDateTime earliestDate;

    public ResidentsInCityAction(DB_OPERATION dbOperation, World.City city, int numResidents, LocalDateTime earliestDate) {
        super(dbOperation);
        this.city = city;
        this.numResidents = numResidents;
        this.earliestDate = earliestDate;
    }

    @Override
    public HashMap<String, Object> outputForReport(List<String> residentEmails) {
        return new HashMap<String, Object>(){{
            put("RESIDENT_EMAILS", residentEmails);
        }};
    }
}
