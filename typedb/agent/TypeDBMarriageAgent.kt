/*
 * Copyright (C) 2022 Vaticle
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
package com.vaticle.typedb.benchmark.typedb.agent

import com.vaticle.typedb.benchmark.common.concept.Country
import com.vaticle.typedb.benchmark.common.concept.Gender
import com.vaticle.typedb.benchmark.common.concept.Marriage
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent
import com.vaticle.typedb.benchmark.simulation.driver.Client
import com.vaticle.typedb.benchmark.typedb.Labels
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typedb.client.api.answer.ConceptMap
import com.vaticle.typeql.lang.TypeQL
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Collectors
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class TypeDBMarriageAgent(client: TypeDBClient, context: Context) : MarriageAgent<TypeDBTransaction>(client, context) {
    override fun matchPartner(
        tx: TypeDBTransaction, country: Country, birthDate: LocalDateTime, gender: Gender
    ): Stream<Person> {
        return tx.query().match(
            TypeQL.match(
                TypeQL.rel(Labels.CONTAINER, Labels.COUNTRY).rel(Labels.CONTAINED, Labels.CITY).isa(Labels.CONTAINS),
                TypeQL.`var`(Labels.COUNTRY).isa(Labels.COUNTRY).has(Labels.CODE, country.code),
                TypeQL.`var`(Labels.CITY).isa(Labels.CITY),
                TypeQL.`var`(Labels.PERSON).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(Labels.EMAIL))
                    .has(Labels.GENDER, gender.value)
                    .has(Labels.BIRTH_DATE, birthDate),
                TypeQL.`var`().rel(Labels.RESIDENCE, TypeQL.`var`(Labels.CITY))
                    .rel(Labels.RESIDENT, TypeQL.`var`(Labels.PERSON))
                    .isa(Labels.RESIDENTSHIP)
            )
        ).map { conceptMap: ConceptMap -> Person(conceptMap[Labels.EMAIL].asAttribute().asString().value) }
    }

    override fun insertMarriage(
        tx: TypeDBTransaction, wifeEmail: String,
        husbandEmail: String, marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage? {
        tx.query().insert(
            TypeQL.match(
                TypeQL.`var`(W).isa(Labels.PERSON).has(Labels.EMAIL, wifeEmail),
                TypeQL.`var`(H).isa(Labels.PERSON).has(Labels.EMAIL, husbandEmail)
            ).insert(
                TypeQL.rel(Labels.WIFE, W).rel(Labels.HUSBAND, H).isa(Labels.MARRIAGE)
                    .has(Labels.MARRIAGE_LICENCE, marriageLicence).has(Labels.MARRIAGE_DATE, marriageDate)
            )
        )
        return if (context.isReporting) report(tx, wifeEmail, husbandEmail, marriageLicence, marriageDate) else null
    }

    private fun report(
        tx: TypeDBTransaction, wifeEmail: String, husbandEmail: String,
        marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage {
        val answers = tx.query().match(
            TypeQL.match(
                TypeQL.`var`(W).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(EW)),
                TypeQL.`var`(EW).eq(wifeEmail),
                TypeQL.`var`(H).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(EH)),
                TypeQL.`var`(EH).eq(husbandEmail),
                TypeQL.rel(Labels.WIFE, W).rel(Labels.HUSBAND, H).isa(Labels.MARRIAGE)
                    .has(Labels.MARRIAGE_LICENCE, TypeQL.`var`(L))
                    .has(Labels.MARRIAGE_DATE, TypeQL.`var`(D)),
                TypeQL.`var`(D).eq(marriageDate),
                TypeQL.`var`(L).eq(marriageLicence)
            )[TypeQL.`var`(EW), TypeQL.`var`(EH), TypeQL.`var`(L), TypeQL.`var`(D)]
        ).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val wife = Person(inserted[EW].asAttribute().asString().value)
        val husband = Person(inserted[EH].asAttribute().asString().value)
        val licence = inserted[L].asAttribute().asString().value
        val date = inserted[D].asAttribute().asDateTime().value
        return Marriage(wife, husband, licence, date)
    }

    companion object {
        private const val W = "w"
        private const val H = "h"
        private const val EW = "ew"
        private const val EH = "eh"
        private const val L = "l"
        private const val D = "d"
    }
}
