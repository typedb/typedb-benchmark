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

import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Gender;
import com.vaticle.typedb.benchmark.common.concept.Marriage;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typeql.lang.TypeQL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CITY;
import static com.vaticle.typedb.benchmark.typedb.Labels.CODE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS;
import static com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY;
import static com.vaticle.typedb.benchmark.typedb.Labels.EMAIL;
import static com.vaticle.typedb.benchmark.typedb.Labels.GENDER;
import static com.vaticle.typedb.benchmark.typedb.Labels.HUSBAND;
import static com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE;
import static com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE_DATE;
import static com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE_LICENSE;
import static com.vaticle.typedb.benchmark.typedb.Labels.PERSON;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENCE;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENT;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENTSHIP;
import static com.vaticle.typedb.benchmark.typedb.Labels.WIFE;
import static com.vaticle.typeql.lang.TypeQL.rel;
import static com.vaticle.typeql.lang.TypeQL.var;
import static java.util.stream.Collectors.toList;

public class TypeDBMarriageAgent extends MarriageAgent<TypeDBTransaction> {

    private static final String W = "w", H = "h";
    private static final String EW = "ew", EH = "eh", L = "l", D = "d";

    public TypeDBMarriageAgent(Client<?, TypeDBTransaction> client, Context context) {
        super(client, context);
    }

    @Override
    protected Stream<Person> matchPartner(TypeDBTransaction tx, Country country, LocalDateTime birthDate, Gender gender) {
        return tx.query().match(TypeQL.match(
                rel(CONTAINER, COUNTRY).rel(CONTAINED, CITY).isa(CONTAINS),
                var(COUNTRY).isa(COUNTRY).has(CODE, country.code()),
                var(CITY).isa(CITY),
                var(PERSON).isa(PERSON).has(EMAIL, var(EMAIL)).has(GENDER, gender.value()).has(BIRTH_DATE, birthDate),
                var().rel(RESIDENCE, var(CITY)).rel(RESIDENT, var(PERSON)).isa(RESIDENTSHIP)))
                .map(conceptMap -> new Person(conceptMap.get(EMAIL).asAttribute().asString().getValue()));
    }

    @Override
    protected Optional<Marriage> insertMarriage(TypeDBTransaction tx, String wifeEmail,
                                                String husbandEmail, String marriageLicence, LocalDateTime marriageDate) {
        tx.query().insert(TypeQL.match(
                var(W).isa(PERSON).has(EMAIL, wifeEmail),
                var(H).isa(PERSON).has(EMAIL, husbandEmail)
        ).insert(
                rel(WIFE, W).rel(HUSBAND, H).isa(MARRIAGE)
                        .has(MARRIAGE_LICENSE, marriageLicence).has(MARRIAGE_DATE, marriageDate)
        ));

        if (context.isReporting()) return report(tx, wifeEmail, husbandEmail, marriageLicence, marriageDate);
        else return Optional.empty();
    }

    private Optional<Marriage> report(TypeDBTransaction tx, String wifeEmail, String husbandEmail,
                                      String marriageLicence, LocalDateTime marriageDate) {
        List<ConceptMap> answers = tx.query().match(TypeQL.match(
                var(W).isa(PERSON).has(EMAIL, var(EW)), var(EW).eq(wifeEmail),
                var(H).isa(PERSON).has(EMAIL, var(EH)), var(EH).eq(husbandEmail),
                rel(WIFE, W).rel(HUSBAND, H).isa(MARRIAGE)
                        .has(MARRIAGE_LICENSE, var(L)), var(L).eq(marriageLicence)
                        .has(MARRIAGE_DATE, var(D)), var(D).eq(marriageDate)
        ).get(var(W), var(H), var(L), var(D)))
                .collect(toList());
        assert answers.size() == 1;
        ConceptMap inserted = answers.get(0);
        Person wife = new Person(inserted.get(EW).asAttribute().asString().getValue());
        Person husband = new Person(inserted.get(EH).asAttribute().asString().getValue());
        String licence = inserted.get(L).asAttribute().asString().getValue();
        LocalDateTime date = inserted.get(D).asAttribute().asDateTime().getValue();
        return Optional.of(new Marriage(wife, husband, licence, date));
    }
}
