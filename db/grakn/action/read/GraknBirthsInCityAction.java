package grakn.simulation.db.grakn.action.read;

import grakn.simulation.db.common.action.read.BirthsInCityAction;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.BORN_IN;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;

public class GraknBirthsInCityAction extends BirthsInCityAction<GraknDbOperationController.TransactionalDbOperation> {
    public GraknBirthsInCityAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation, city, today);
    }

    @Override
    public List<String> run() {
        GraqlGet.Unfiltered childrenQuery = Graql.match(
                Graql.var("c").isa(CITY)
                        .has(LOCATION_NAME, worldCity.name()),
                Graql.var("child").isa(PERSON)
                        .has(EMAIL, Graql.var(EMAIL))
                        .has(DATE_OF_BIRTH, today),
                Graql.var("bi").isa(BORN_IN)
                        .rel(BORN_IN_PLACE_OF_BIRTH, "c")
                        .rel(BORN_IN_CHILD, "child")
        ).get();
        return dbOperation.tx().getOrderedAttribute(childrenQuery, EMAIL, null);
    }
}
