/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.grakn.action.insight;

import grakn.benchmark.common.action.insight.FourHopAction;
import grakn.benchmark.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlMatch;

import java.util.List;

import static grakn.benchmark.grakn.action.Model.BORN_IN;
import static grakn.benchmark.grakn.action.Model.BORN_IN_CHILD;
import static grakn.benchmark.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.COMPANY;
import static grakn.benchmark.grakn.action.Model.COMPANY_NAME;
import static grakn.benchmark.grakn.action.Model.EMPLOYMENT;
import static grakn.benchmark.grakn.action.Model.EMPLOYMENT_EMPLOYEE;
import static grakn.benchmark.grakn.action.Model.EMPLOYMENT_EMPLOYER;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PARENTSHIP;
import static grakn.benchmark.grakn.action.Model.PARENTSHIP_CHILD;
import static grakn.benchmark.grakn.action.Model.PARENTSHIP_PARENT;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static grakn.benchmark.grakn.action.Model.TRANSACTION;
import static grakn.benchmark.grakn.action.Model.TRANSACTION_BUYER;
import static grakn.benchmark.grakn.action.Model.TRANSACTION_SELLER;

public class GraknFourHopAction extends FourHopAction<GraknOperation> {
    public GraknFourHopAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(query(), COMPANY_NAME, null);
    }

    public static GraqlMatch.Unfiltered query() {
        return Graql.match(
                Graql.var(CITY).isa(CITY).has(LOCATION_NAME, "London"),
                Graql.var().rel(BORN_IN_PLACE_OF_BIRTH, Graql.var(CITY)).rel(BORN_IN_CHILD, Graql.var("child")).isa(BORN_IN),
                Graql.var("child").isa(PERSON),
                Graql.var().rel(PARENTSHIP_PARENT, Graql.var("parent")).rel(PARENTSHIP_CHILD, Graql.var("child")).isa(PARENTSHIP),
                Graql.var("parent").isa(PERSON),
                Graql.var().rel(EMPLOYMENT_EMPLOYEE, Graql.var("parent")).rel(EMPLOYMENT_EMPLOYER, Graql.var("buyer")).isa(EMPLOYMENT),
                Graql.var("buyer").isa(COMPANY),
                Graql.var().rel(TRANSACTION_BUYER, Graql.var("buyer")).rel(TRANSACTION_SELLER, Graql.var("seller")).isa(TRANSACTION),
                Graql.var("seller").isa(COMPANY).has(COMPANY_NAME, Graql.var(COMPANY_NAME))
        );
    }
}
