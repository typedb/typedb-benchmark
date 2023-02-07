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
import com.vaticle.typedb.benchmark.common.concept.Marriage
import com.vaticle.typedb.benchmark.common.concept.Parenthood
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.simulation.agent.ParenthoodAgent
import com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_PLACE
import com.vaticle.typedb.benchmark.typedb.Labels.CHILD
import com.vaticle.typedb.benchmark.typedb.Labels.CITY
import com.vaticle.typedb.benchmark.typedb.Labels.CODE
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS
import com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY
import com.vaticle.typedb.benchmark.typedb.Labels.EMAIL
import com.vaticle.typedb.benchmark.typedb.Labels.HUSBAND
import com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE
import com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE_DATE
import com.vaticle.typedb.benchmark.typedb.Labels.MARRIAGE_LICENCE
import com.vaticle.typedb.benchmark.typedb.Labels.PARENT
import com.vaticle.typedb.benchmark.typedb.Labels.PARENTHOOD
import com.vaticle.typedb.benchmark.typedb.Labels.PERSON
import com.vaticle.typedb.benchmark.typedb.Labels.PLACE
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENCE
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENT
import com.vaticle.typedb.benchmark.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.benchmark.typedb.Labels.WIFE
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typedb.client.api.answer.ConceptMap
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import java.time.LocalDateTime
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class TypeDBParenthoodAgent(client: TypeDBClient, context: Context) : ParenthoodAgent<TypeDBTransaction>(client, context) {
    override fun matchNewborns(tx: TypeDBTransaction, country: Country, today: LocalDateTime): Stream<Person> {
        return tx.query().match(match(
            `var`(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            rel(CONTAINER, COUNTRY).rel(CONTAINED, CITY).isa(CONTAINS),
            `var`(CITY).isa(CITY),
            `var`(PERSON).isa(PERSON).has(EMAIL, `var`(EMAIL)).has(BIRTH_DATE, today),
            rel(PLACE, `var`(CITY)).rel(CHILD, PERSON).isa(BIRTH_PLACE)
        )).map { conceptMap: ConceptMap -> Person(email = conceptMap[EMAIL].asAttribute().asString().value) }
    }

    override fun matchMarriages(tx: TypeDBTransaction, country: Country, marriageDate: LocalDateTime): Stream<Marriage> {
        return tx.query().match(match(
            `var`(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            rel(CONTAINER, COUNTRY).rel(CONTAINED, CITY).isa(CONTAINS),
            `var`(CITY).isa(CITY),
            `var`(W).isa(PERSON).has(EMAIL, `var`(EW)),
            `var`(H).isa(PERSON).has(EMAIL, `var`(EH)),
            rel(WIFE, W).rel(HUSBAND, H).isa(MARRIAGE)
                .has(MARRIAGE_DATE, marriageDate)
                .has(MARRIAGE_LICENCE, `var`(MARRIAGE_LICENCE)),
            rel(RESIDENCE, `var`(CITY)).rel(RESIDENT, W).isa(RESIDENTSHIP)
        )).map { conceptMap: ConceptMap ->
            Marriage(
                wife = Person(email = conceptMap[EW].asAttribute().asString().value),
                husband = Person(email = conceptMap[EH].asAttribute().asString().value),
                licence = conceptMap[MARRIAGE_LICENCE].asAttribute().asString().value,
                date = marriageDate
            )
        }
    }

    override fun insertParenthood(
        tx: TypeDBTransaction, motherEmail: String, fatherEmail: String, childEmail: String
    ): Parenthood? {
        tx.query().insert(
            match(
                `var`(M).isa(PERSON).has(EMAIL, motherEmail),
                `var`(F).isa(PERSON).has(EMAIL, fatherEmail),
                `var`(C).isa(PERSON).has(EMAIL, childEmail)
            ).insert(
                rel(PARENT, M).rel(PARENT, F).rel(CHILD, C).isa(PARENTHOOD)
            )
        )
        return if (context.isReporting) report(tx, motherEmail, fatherEmail, childEmail) else null
    }

    private fun report(tx: TypeDBTransaction, motherEmail: String, fatherEmail: String, childEmail: String): Parenthood {
        val answers = tx.query().match(
            match(
                `var`(M).isa(PERSON).has(EMAIL, `var`(EM)),
                `var`(EM).eq(motherEmail),
                `var`(F).isa(PERSON).has(EMAIL, `var`(EF)),
                `var`(EF).eq(fatherEmail),
                `var`(C).isa(PERSON).has(EMAIL, `var`(EC)),
                `var`(EC).eq(childEmail),
                rel(PARENT, M).rel(PARENT, F).rel(CHILD, C).isa(PARENTHOOD)
            )[`var`(EM), `var`(EF), `var`(EC)]
        ).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val mother = Person(email = inserted[EM].asAttribute().asString().value)
        val father = Person(email = inserted[EF].asAttribute().asString().value)
        val child = Person(email = inserted[EC].asAttribute().asString().value)
        return Parenthood(mother, father, child)
    }

    companion object {
        private const val W = "w"
        private const val H = "h"
        private const val EW = "ew"
        private const val EH = "eh"
        private const val M = "m"
        private const val F = "f"
        private const val C = "c"
        private const val EM = "em"
        private const val EF = "ef"
        private const val EC = "ec"
    }
}
