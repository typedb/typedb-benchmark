package grakn.simulation.db.grakn.action.read;

import grakn.simulation.db.common.action.read.CompaniesInContinentAction;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.COMPANY;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.COUNTRY;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION_INCORPORATED;
import static grakn.simulation.db.grakn.schema.Schema.INCORPORATION_INCORPORATING;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_HIERARCHY;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;

public class GraknCompaniesInContinentAction extends CompaniesInContinentAction<GraknDbOperationController.TransactionalDbOperation> {
    public GraknCompaniesInContinentAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, World.Continent continent) {
        super(dbOperation, continent);
    }

    @Override
    public List<Long> run() {
        GraqlGet.Unfiltered query = Graql.match(
                Graql.var(CONTINENT).isa(CONTINENT)
                        .has(LOCATION_NAME, continent.name()),
                Graql.var(LOCATION_HIERARCHY).isa(LOCATION_HIERARCHY).rel(COUNTRY).rel(CONTINENT),
                Graql.var(COUNTRY).isa(COUNTRY),
                Graql.var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NUMBER, Graql.var(COMPANY_NUMBER)),
                Graql.var(INCORPORATION).isa(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, Graql.var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, Graql.var(COUNTRY))
        ).get();
        return dbOperation.tx().getOrderedAttribute(query, COMPANY_NUMBER, null);
    }
}
