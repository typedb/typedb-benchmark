package grakn.simulation.db.common.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.util.ArrayList;

public abstract class InsertProductAction<DB_OPERATION extends DbOperationController.DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final World.Continent continent;
    protected final Double barcode;
    protected final String productName;
    protected final String productDescription;

    public InsertProductAction(DB_OPERATION dbOperation, World.Continent continent, Double barcode, String productName, String productDescription) {
        super(dbOperation);
        this.continent = continent;
        this.barcode = barcode;
        this.productName = productName;
        this.productDescription = productDescription;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(continent, barcode, productName, productDescription);
    }
}
