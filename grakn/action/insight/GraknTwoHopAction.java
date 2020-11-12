package grakn.simulation.grakn.action.insight;

import grakn.simulation.common.action.insight.TwoHopAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.grakn.action.Model.BORN_IN;
import static grakn.simulation.grakn.action.Model.BORN_IN_CHILD;
import static grakn.simulation.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PARENTSHIP;
import static grakn.simulation.grakn.action.Model.PARENTSHIP_CHILD;
import static grakn.simulation.grakn.action.Model.PARENTSHIP_PARENT;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknTwoHopAction extends TwoHopAction<GraknOperation> {
    public GraknTwoHopAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(query(), EMAIL, null);
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(CITY).isa(CITY).has(LOCATION_NAME, "London"),
                    Graql.var().isa(BORN_IN).rel(BORN_IN_PLACE_OF_BIRTH, Graql.var(CITY)).rel(BORN_IN_CHILD, Graql.var("child")),
                    Graql.var("child").isa(PERSON),
                    Graql.var().isa(PARENTSHIP).rel(PARENTSHIP_PARENT, Graql.var("parent")).rel(PARENTSHIP_CHILD, Graql.var("child")),
                    Graql.var("parent").isa(PERSON).has(EMAIL, Graql.var(EMAIL))
            ).get();
    }
}
