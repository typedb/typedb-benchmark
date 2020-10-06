package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class ProductsInContinentAction<DB_OPERATION extends DbOperationController.DbOperation> extends ReadAction<DB_OPERATION, List<Double>> {
    protected final World.Continent continent;

    public ProductsInContinentAction(DB_OPERATION dbOperation, World.Continent continent) {
        super(dbOperation);
        this.continent = continent;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(continent);
    }
}
