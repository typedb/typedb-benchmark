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

class TypeDBParenthoodAgent(client: TypeDBClient, context: Context) : ParenthoodAgent<TypeDBTransaction>(client, context) {
    override fun matchNewborns(tx: TypeDBTransaction, country: Country, today: LocalDateTime): Stream<Person> {
        return tx.query().match(
            TypeQL.match(
                TypeQL.`var`(Labels.COUNTRY).isa(Labels.COUNTRY).has(Labels.CODE, country.code),
                TypeQL.rel(Labels.CONTAINER, Labels.COUNTRY).rel(Labels.CONTAINED, Labels.CITY).isa(Labels.CONTAINS),
                TypeQL.`var`(Labels.CITY).isa(Labels.CITY),
                TypeQL.`var`(Labels.PERSON).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(Labels.EMAIL))
                    .has(Labels.BIRTH_DATE, today),
                TypeQL.rel(Labels.PLACE, TypeQL.`var`(Labels.CITY)).rel(Labels.CHILD, Labels.PERSON)
                    .isa(Labels.BIRTH_PLACE)
            )
        ).map { conceptMap: ConceptMap -> Person(conceptMap[Labels.EMAIL].asAttribute().asString().value) }
    }

    override fun matchMarriages(tx: TypeDBTransaction, country: Country, marriageDate: LocalDateTime): Stream<Marriage> {
        return tx.query().match(
            TypeQL.match(
                TypeQL.`var`(Labels.COUNTRY).isa(Labels.COUNTRY).has(Labels.CODE, country.code),
                TypeQL.rel(Labels.CONTAINER, Labels.COUNTRY).rel(Labels.CONTAINED, Labels.CITY).isa(Labels.CONTAINS),
                TypeQL.`var`(Labels.CITY).isa(Labels.CITY),
                TypeQL.`var`(W).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(EW)),
                TypeQL.`var`(H).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(EH)),
                TypeQL.rel(Labels.WIFE, W).rel(Labels.HUSBAND, H).isa(Labels.MARRIAGE)
                    .has(Labels.MARRIAGE_DATE, marriageDate)
                    .has(Labels.MARRIAGE_LICENCE, TypeQL.`var`(Labels.MARRIAGE_LICENCE)),
                TypeQL.rel(Labels.RESIDENCE, TypeQL.`var`(Labels.CITY)).rel(Labels.RESIDENT, W).isa(Labels.RESIDENTSHIP)
            )
        ).map { conceptMap: ConceptMap ->
            Marriage(
                Person(conceptMap[EW].asAttribute().asString().value),
                Person(conceptMap[EH].asAttribute().asString().value),
                conceptMap[Labels.MARRIAGE_LICENCE].asAttribute().asString().value,
                marriageDate
            )
        }
    }

    override fun insertParenthood(
        tx: TypeDBTransaction, motherEmail: String, fatherEmail: String, childEmail: String
    ): Parenthood? {
        tx.query().insert(
            TypeQL.match(
                TypeQL.`var`(M).isa(Labels.PERSON).has(Labels.EMAIL, motherEmail),
                TypeQL.`var`(F).isa(Labels.PERSON).has(Labels.EMAIL, fatherEmail),
                TypeQL.`var`(C).isa(Labels.PERSON).has(Labels.EMAIL, childEmail)
            ).insert(
                TypeQL.rel(Labels.PARENT, M).rel(Labels.PARENT, F).rel(Labels.CHILD, C).isa(Labels.PARENTHOOD)
            )
        )
        return if (context.isReporting) report(tx, motherEmail, fatherEmail, childEmail) else null
    }

    private fun report(tx: TypeDBTransaction, motherEmail: String, fatherEmail: String, childEmail: String): Parenthood {
        val answers = tx.query().match(
            TypeQL.match(
                TypeQL.`var`(M).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(EM)),
                TypeQL.`var`(EM).eq(motherEmail),
                TypeQL.`var`(F).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(EF)),
                TypeQL.`var`(EF).eq(fatherEmail),
                TypeQL.`var`(C).isa(Labels.PERSON).has(Labels.EMAIL, TypeQL.`var`(EC)),
                TypeQL.`var`(EC).eq(childEmail),
                TypeQL.rel(Labels.PARENT, M).rel(Labels.PARENT, F).rel(Labels.CHILD, C).isa(Labels.PARENTHOOD)
            )[TypeQL.`var`(EM), TypeQL.`var`(EF), TypeQL.`var`(EC)]
        ).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val mother = Person(inserted[EM].asAttribute().asString().value)
        val father = Person(inserted[EF].asAttribute().asString().value)
        val child = Person(inserted[EC].asAttribute().asString().value)
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
