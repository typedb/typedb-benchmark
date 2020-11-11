package grakn.simulation.common.action.read;

import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class ProductsInContinentAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, List<Double>> {
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
