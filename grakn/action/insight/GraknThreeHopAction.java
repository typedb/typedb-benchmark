package grakn.simulation.grakn.action.insight;

import grakn.simulation.common.action.insight.ThreeHopAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.grakn.action.Model.BORN_IN;
import static grakn.simulation.grakn.action.Model.BORN_IN_CHILD;
import static grakn.simulation.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.COMPANY;
import static grakn.simulation.grakn.action.Model.COMPANY_NAME;
import static grakn.simulation.grakn.action.Model.EMPLOYMENT;
import static grakn.simulation.grakn.action.Model.EMPLOYMENT_EMPLOYEE;
import static grakn.simulation.grakn.action.Model.EMPLOYMENT_EMPLOYER;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PARENTSHIP;
import static grakn.simulation.grakn.action.Model.PARENTSHIP_CHILD;
import static grakn.simulation.grakn.action.Model.PARENTSHIP_PARENT;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknThreeHopAction extends ThreeHopAction<GraknOperation> {
    public GraknThreeHopAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(query(), COMPANY_NAME, null);
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(CITY).isa(CITY).has(LOCATION_NAME, "London"),
                    Graql.var().isa(BORN_IN).rel(BORN_IN_PLACE_OF_BIRTH, Graql.var(CITY)).rel(BORN_IN_CHILD, Graql.var("child")),
                    Graql.var("child").isa(PERSON),
                    Graql.var().isa(PARENTSHIP).rel(PARENTSHIP_PARENT, Graql.var("parent")).rel(PARENTSHIP_CHILD, Graql.var("child")),
                    Graql.var("parent").isa(PERSON),
                    Graql.var().isa(EMPLOYMENT).rel(EMPLOYMENT_EMPLOYEE, Graql.var("parent")).rel(EMPLOYMENT_EMPLOYER, Graql.var(COMPANY)),
                    Graql.var(COMPANY).isa(COMPANY).has(COMPANY_NAME, Graql.var(COMPANY_NAME))
            ).get();
    }
}
