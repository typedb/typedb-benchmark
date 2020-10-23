package grakn.simulation.db.grakn.action.insight;

import grakn.simulation.db.common.action.insight.FourHopAction;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.BORN_IN;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY_NAME;
import static grakn.simulation.db.grakn.schema.Schema.EMPLOYMENT;
import static grakn.simulation.db.grakn.schema.Schema.EMPLOYMENT_EMPLOYEE;
import static grakn.simulation.db.grakn.schema.Schema.EMPLOYMENT_EMPLOYER;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP_PARENT;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.TRANSACTION;
import static grakn.simulation.db.grakn.schema.Schema.TRANSACTION_BUYER;
import static grakn.simulation.db.grakn.schema.Schema.TRANSACTION_SELLER;

public class GraknFourHopAction extends FourHopAction<GraknOperation> {
    public GraknFourHopAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.getOrderedAttribute(query(), COMPANY_NAME, null);
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(CITY).isa(CITY).has(LOCATION_NAME, "London"),
                    Graql.var().isa(BORN_IN).rel(BORN_IN_PLACE_OF_BIRTH, Graql.var(CITY)).rel(BORN_IN_CHILD, Graql.var("child")),
                    Graql.var("child").isa(PERSON),
                    Graql.var().isa(PARENTSHIP).rel(PARENTSHIP_PARENT, Graql.var("parent")).rel(PARENTSHIP_CHILD, Graql.var("child")),
                    Graql.var("parent").isa(PERSON),
                    Graql.var().isa(EMPLOYMENT).rel(EMPLOYMENT_EMPLOYEE, Graql.var("parent")).rel(EMPLOYMENT_EMPLOYER, Graql.var("buyer")),
                    Graql.var("buyer").isa(COMPANY),
                    Graql.var().isa(TRANSACTION).rel(TRANSACTION_BUYER, Graql.var("buyer")).rel(TRANSACTION_SELLER, Graql.var("seller")),
                    Graql.var("seller").isa(COMPANY).has(COMPANY_NAME, Graql.var(COMPANY_NAME))
            ).get();
    }
}
