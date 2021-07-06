/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.benchmark.typedb.agent;

import com.vaticle.typedb.benchmark.common.concept.City;
import com.vaticle.typedb.benchmark.common.concept.Marriage;
import com.vaticle.typedb.benchmark.common.concept.Parentship;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.simulation.agent.ParentshipAgent;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typeql.lang.TypeQL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE;
import static com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_PLACE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CHILD;
import static com.vaticle.typedb.benchmark.typedb.Labels.CITY;
import static com.vaticle.typedb.benchmark.typedb.Labels.CODE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS;
import static com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY;
import static com.vaticle.typedb.benchmark.typedb.Labels.EMAIL;
import static com.vaticle.typedb.benchmark.typedb.Labels.HUSBAND;
import static com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE;
import static com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE_LICENSE;
import static com.vaticle.typedb.benchmark.typedb.Labels.PARENT;
import static com.vaticle.typedb.benchmark.typedb.Labels.PARENTSHIP;
import static com.vaticle.typedb.benchmark.typedb.Labels.PERSON;
import static com.vaticle.typedb.benchmark.typedb.Labels.PLACE;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENCE;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENT;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENTSHIP;
import static com.vaticle.typedb.benchmark.typedb.Labels.WIFE;
import static com.vaticle.typeql.lang.TypeQL.rel;
import static com.vaticle.typeql.lang.TypeQL.var;
import static java.util.stream.Collectors.toList;

public class TypeDBParentshipAgent extends ParentshipAgent<TypeDBTransaction> {

    private static final String W = "w", H = "h";
    private static final String EW = "ew", EH = "eh";
    private static final String M = "m", F = "f", C = "c";
    private static final String EM = "em", EF = "ef", EC = "ec";

    protected TypeDBParentshipAgent(Client<?, TypeDBTransaction> client, Context context) {
        super(client, context);
    }

    @Override
    protected Stream<Person> matchNewborns(TypeDBTransaction tx, City city, LocalDateTime today) {
        return tx.query().match(TypeQL.match(
                var(CITY).isa(CITY).has(CODE, city.code()),
                var(PERSON).isa(PERSON).has(EMAIL, EMAIL).has(BIRTH_DATE, today),
                rel(PLACE, var(CITY)).rel(CHILD, PERSON).isa(BIRTH_PLACE),
                rel(RESIDENCE, var(CITY)).rel(RESIDENT, PERSON).isa(RESIDENTSHIP)
        )).map(conceptMap -> new Person(conceptMap.get(EMAIL).asAttribute().asString().getValue()));
    }

    @Override
    protected Stream<Marriage> matchMarriages(TypeDBTransaction tx, City city, LocalDateTime marriageDate) {
        return tx.query().match(TypeQL.match(
                rel(CONTAINER, COUNTRY).rel(CONTAINED, CITY).isa(CONTAINS),
                var(COUNTRY).isa(COUNTRY),
                var(CITY).isa(CITY).has(CODE, city.code()),
                var(W).isa(PERSON).has(EMAIL, EW),
                var(H).isa(PERSON).has(EMAIL, EH),
                rel(WIFE, W).rel(HUSBAND, H).isa(MARRIAGE).has(MARRIAGE_LICENSE, MARRIAGE_LICENSE),
                rel(RESIDENCE, var(CITY)).rel(RESIDENT, W).isa(RESIDENTSHIP)
        )).map(conceptMap -> new Marriage(
                new Person(conceptMap.get(EW).asAttribute().asString().getValue()),
                new Person(conceptMap.get(EH).asAttribute().asString().getValue()),
                conceptMap.get(MARRIAGE_LICENSE).asAttribute().asString().getValue()));
    }

    @Override
    protected Optional<Parentship> insertParentShip(TypeDBTransaction tx, String motherEmail, String fatherEmail,
                                                    String childEmail) {
        tx.query().insert(TypeQL.match(
                var(M).isa(PERSON).has(EMAIL, motherEmail),
                var(F).isa(PERSON).has(EMAIL, fatherEmail),
                var(C).isa(PERSON).has(EMAIL, childEmail)
        ).insert(
                rel(PARENT, M).rel(PARENT, F).rel(CHILD, C).isa(PARENTSHIP)
        ));
        if (context.isReporting()) return report(tx, motherEmail, fatherEmail, childEmail);
        else return Optional.empty();
    }

    private Optional<Parentship> report(TypeDBTransaction tx, String motherEmail, String fatherEmail, String childEmail) {
        List<ConceptMap> answers = tx.query().match(TypeQL.match(
                var(M).isa(PERSON).has(EMAIL, EM), var(EM).eq(motherEmail),
                var(F).isa(PERSON).has(EMAIL, EF), var(EF).eq(fatherEmail),
                var(C).isa(PERSON).has(EMAIL, EC), var(EC).eq(childEmail),
                rel(PARENT, M).rel(PARENT, F).rel(CHILD, C).isa(PARENTSHIP)
        ).get(var(EM), var(EF), var(EC))).collect(toList());
        assert answers.size() == 1;
        ConceptMap inserted = answers.get(0);
        Person mother = new Person(inserted.get(EM).asAttribute().asString().getValue());
        Person father = new Person(inserted.get(EF).asAttribute().asString().getValue());
        Person child = new Person(inserted.get(EC).asAttribute().asString().getValue());
        return Optional.of(new Parentship(mother, father, child));
    }

}
