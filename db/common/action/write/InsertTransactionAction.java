package grakn.simulation.db.common.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.utils.Pair;
import grakn.simulation.db.common.world.World;

import java.util.ArrayList;

public abstract class InsertTransactionAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final World.Continent continent;
    protected final Pair<Long, Double> transaction;
    protected final Long sellerCompanyNumber;
    protected final double value;
    protected final int productQuantity;
    protected final boolean isTaxable;

    public InsertTransactionAction(DB_OPERATION dbOperation, World.Continent continent, Pair<Long, Double> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        super(dbOperation);
        this.continent = continent;
        this.transaction = transaction;
        this.sellerCompanyNumber = sellerCompanyNumber;
        this.value = value;
        this.productQuantity = productQuantity;
        this.isTaxable = isTaxable;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(continent, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
    }

    public enum InsertTransactionActionField implements ComparableField {
        SELLER, BUYER, MERCHANDISE, VALUE, PRODUCT_QUANTITY, IS_TAXABLE, CONTINENT
    }
}
