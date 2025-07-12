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

import com.vaticle.typedb.driver.api.TypeDBSession
import com.vaticle.typedb.driver.api.TypeDBTransaction
import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.common.concept.Marriage
import com.vaticle.typedb.iam.simulation.common.concept.Parenthood
import com.vaticle.typedb.iam.simulation.common.concept.Person
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.agent.ParenthoodAgent
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.BIRTH_PLACE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CHILD
import com.vaticle.typedb.iam.simulation.typedb.Labels.CITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINED
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINER
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINS
import com.vaticle.typedb.iam.simulation.typedb.Labels.COUNTRY
import com.vaticle.typedb.iam.simulation.typedb.Labels.EMAIL
import com.vaticle.typedb.iam.simulation.typedb.Labels.HUSBAND
import com.vaticle.typedb.iam.simulation.typedb.Labels.MARRIAGE
import com.vaticle.typedb.iam.simulation.typedb.Labels.MARRIAGE_DATE
import com.vaticle.typedb.iam.simulation.typedb.Labels.MARRIAGE_LICENCE
import com.vaticle.typedb.iam.simulation.typedb.Labels.PARENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.PARENTHOOD
import com.vaticle.typedb.iam.simulation.typedb.Labels.PERSON
import com.vaticle.typedb.iam.simulation.typedb.Labels.PLACE
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENCE
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.RESIDENTSHIP
import com.vaticle.typedb.iam.simulation.typedb.Labels.WIFE
import com.vaticle.typedb.driver.api.answer.ConceptMap
import com.vaticle.typedb.benchmark.framework.common.seed.RandomSource
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBSessionEx.writeTransaction
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBDriver
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.cVar
import java.time.LocalDateTime
import java.util.Comparator
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class TypeDBParenthoodAgent(client: TypeDBDriver, context: Context) : ParenthoodAgent<TypeDBSession>(client, context) {

    override val actionHandlers = mapOf(
        "doAction" to ::doAction,
    )

    fun doAction(session: TypeDBSession, partition: Country, random: RandomSource): List<Report> {
        val reports: MutableList<Report> = ArrayList()
        session.writeTransaction().use { tx ->
            val marriageDate = context.today().minusYears(context.model.yearsBeforeParenthood.toLong())
            val marriages = matchMarriages(tx, partition, marriageDate).sorted(Comparator.comparing { it.licence }).collect(toList())
            val newBorns = matchNewborns(tx, partition, context.today()).sorted(Comparator.comparing { it.email }).collect(toList())
            val parenthoods = random.randomAllocation(marriages, newBorns)
            parenthoods.forEach { (marriage, person) ->
                val wife = marriage.wife.email
                val husband = marriage.husband.email
                val child = person.email
                val inserted = insertParenthood(tx, wife, husband, child)
                if (context.isReporting) {
                    requireNotNull(inserted)
                    reports.add(Report(input = listOf(wife, husband, child), output = listOf(inserted)))
                } else assert(inserted == null)
            }
            tx.commit()
        }
        return reports
    }

    private fun matchNewborns(tx: TypeDBTransaction, country: Country, today: LocalDateTime): Stream<Person> {
        return tx.query().get(match(
            cVar(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            rel(CONTAINER, cVar(COUNTRY)).rel(CONTAINED, cVar(CITY)).isa(CONTAINS),
            cVar(CITY).isa(CITY),
            cVar(PERSON).isa(PERSON).has(EMAIL, cVar(EMAIL)).has(BIRTH_DATE, today),
            rel(PLACE, cVar(CITY)).rel(CHILD, cVar(PERSON)).isa(BIRTH_PLACE)
        ).get()).map { conceptMap: ConceptMap -> Person(email = conceptMap[EMAIL].asAttribute().value.asString()) }
    }

    private fun matchMarriages(tx: TypeDBTransaction, country: Country, marriageDate: LocalDateTime): Stream<Marriage> {
        return tx.query().get(match(
            cVar(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            rel(CONTAINER, cVar(COUNTRY)).rel(CONTAINED, cVar(CITY)).isa(CONTAINS),
            cVar(CITY).isa(CITY),
            cVar(W).isa(PERSON).has(EMAIL, cVar(EW)),
            cVar(H).isa(PERSON).has(EMAIL, cVar(EH)),
            rel(WIFE, cVar(W)).rel(HUSBAND, cVar(H)).isa(MARRIAGE)
                .has(MARRIAGE_DATE, marriageDate)
                .has(MARRIAGE_LICENCE, cVar(MARRIAGE_LICENCE)),
            rel(RESIDENCE, cVar(CITY)).rel(RESIDENT, cVar(W)).isa(RESIDENTSHIP)
        ).get()).map { conceptMap: ConceptMap ->
            Marriage(
                wife = Person(email = conceptMap[EW].asAttribute().value.asString()),
                husband = Person(email = conceptMap[EH].asAttribute().value.asString()),
                licence = conceptMap[MARRIAGE_LICENCE].asAttribute().value.asString(),
                date = marriageDate
            )
        }
    }

    private fun insertParenthood(
        tx: TypeDBTransaction, motherEmail: String, fatherEmail: String, childEmail: String
    ): Parenthood? {
        tx.query().insert(
            match(
                cVar(M).isa(PERSON).has(EMAIL, motherEmail),
                cVar(F).isa(PERSON).has(EMAIL, fatherEmail),
                cVar(C).isa(PERSON).has(EMAIL, childEmail)
            ).insert(
                rel(PARENT, cVar(M)).rel(PARENT, cVar(F)).rel(CHILD, cVar(C)).isa(PARENTHOOD)
            )
        )
        return if (context.isReporting) report(tx, motherEmail, fatherEmail, childEmail) else null
    }

    private fun report(tx: TypeDBTransaction, motherEmail: String, fatherEmail: String, childEmail: String): Parenthood {
        val answers = tx.query().get(
            match(
                cVar(M).isa(PERSON).has(EMAIL, cVar(EM)),
                cVar(EM).eq(motherEmail),
                cVar(F).isa(PERSON).has(EMAIL, cVar(EF)),
                cVar(EF).eq(fatherEmail),
                cVar(C).isa(PERSON).has(EMAIL, cVar(EC)),
                cVar(EC).eq(childEmail),
                rel(PARENT, cVar(M)).rel(PARENT, cVar(F)).rel(CHILD, cVar(C)).isa(PARENTHOOD)
            )[cVar(EM), cVar(EF), cVar(EC)]
        ).collect(toList())
        assert(answers.size == 1)
        val inserted = answers[0]
        val mother = Person(email = inserted[EM].asAttribute().value.asString())
        val father = Person(email = inserted[EF].asAttribute().value.asString())
        val child = Person(email = inserted[EC].asAttribute().value.asString())
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
