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
package com.vaticle.typedb.iam.simulation.typedb.agent

import com.vaticle.typedb.client.api.TypeDBSession
import com.vaticle.typedb.client.api.TypeDBTransaction
import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.common.concept.Gender
import com.vaticle.typedb.iam.simulation.common.concept.Marriage
import com.vaticle.typedb.iam.simulation.common.concept.Person
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.agent.MarriageAgent
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINED
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINER
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINS
import com.vaticle.typedb.iam.simulation.typedb.Labels.COUNTRY
import com.vaticle.typedb.iam.simulation.typedb.Labels.EMAIL
import com.vaticle.typedb.iam.simulation.typedb.Labels.GENDER
import com.vaticle.typedb.iam.simulation.typedb.Labels.HUSBAND
import com.vaticle.typedb.iam.simulation.typedb.Labels.MARRIAGE
import com.vaticle.typedb.iam.simulation.typedb.Labels.MARRIAGE_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.MARRIAGE_LICENCE
import com.vaticle.typedb.iam.simulation.typedb.Labels.PERSON
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENCE
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.iam.simulation.typedb.Labels.WIFE
import com.vaticle.typedb.client.api.answer.ConceptMap
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typedb.simulation.typedb.TypeDBSessionEx.writeTransaction
import com.vaticle.typedb.simulation.typedb.TypeDBClient
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import java.time.LocalDateTime
import java.util.Comparator
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class TypeDBMarriageAgent(client: TypeDBClient, context: Context) : MarriageAgent<TypeDBSession>(client, context) {

    override fun run(session: TypeDBSession, partition: Country, random: RandomSource): List<Report> {
        val reports = mutableListOf<Report>()
        session.writeTransaction().use { tx ->
            val partnerBirthDate = context.today().minusYears(context.model.ageOfAdulthood.toLong())
            val women = matchPartner(tx, partition, partnerBirthDate,
                Gender.FEMALE
            ).sorted(Comparator.comparing { it.email }).collect(toList())
            val men = matchPartner(tx, partition, partnerBirthDate,
                Gender.MALE
            ).sorted(Comparator.comparing { it.email }).collect(toList())
            random.randomPairs(women, men).forEach { (woman, man) ->
                val licence = woman.email + man.email
                val inserted = insertMarriage(tx, woman.email, man.email, licence, context.today())
                if (context.isReporting) {
                    requireNotNull(inserted)
                    reports.add(Report(
                        input = listOf(woman.email, man.email, licence, context.today()),
                        output = listOf(inserted)
                    ))
                } else assert(inserted == null)
            }
            tx.commit()
        }
        return reports
    }

    private fun matchPartner(
        tx: TypeDBTransaction, country: Country, birthDate: LocalDateTime, gender: Gender
    ): Stream<Person> {
        return tx.query().match(match(
            rel(CONTAINER, COUNTRY).rel(CONTAINED, CITY).isa(CONTAINS),
            `var`(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            `var`(CITY).isa(CITY),
            `var`(PERSON).isa(PERSON).has(EMAIL, `var`(EMAIL)).has(GENDER, gender.value).has(BIRTH_DATE, birthDate),
            `var`().rel(RESIDENCE, `var`(CITY)).rel(RESIDENT, `var`(PERSON)).isa(RESIDENTSHIP)
        )).map { conceptMap: ConceptMap -> Person(email = conceptMap[EMAIL].asAttribute().asString().value) }
    }

    private fun insertMarriage(
        tx: TypeDBTransaction, wifeEmail: String,
        husbandEmail: String, marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage? {
        tx.query().insert(
            match(
                `var`(W).isa(PERSON).has(EMAIL, wifeEmail),
                `var`(H).isa(PERSON).has(EMAIL, husbandEmail)
            ).insert(
                rel(WIFE, W).rel(HUSBAND, H).isa(MARRIAGE)
                    .has(MARRIAGE_LICENCE, marriageLicence).has(MARRIAGE_DATE, marriageDate)
            )
        )
        return if (context.isReporting) report(tx, wifeEmail, husbandEmail, marriageLicence, marriageDate) else null
    }

    private fun report(
        tx: TypeDBTransaction, wifeEmail: String, husbandEmail: String,
        marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage {
        val answers = tx.query().match(
            match(
                `var`(W).isa(PERSON).has(EMAIL, `var`(EW)),
                `var`(EW).eq(wifeEmail),
                `var`(H).isa(PERSON).has(EMAIL, `var`(EH)),
                `var`(EH).eq(husbandEmail),
                rel(WIFE, W).rel(HUSBAND, H).isa(MARRIAGE)
                    .has(MARRIAGE_LICENCE, `var`(L))
                    .has(MARRIAGE_DATE, `var`(D)),
                `var`(D).eq(marriageDate),
                `var`(L).eq(marriageLicence)
            )[`var`(EW), `var`(EH), `var`(L), `var`(D)]
        ).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val wife = Person(email = inserted[EW].asAttribute().asString().value)
        val husband = Person(email = inserted[EH].asAttribute().asString().value)
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
