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
package com.vaticle.typedb.iam.simulation.neo4j.agent

import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.common.concept.Marriage
import com.vaticle.typedb.iam.simulation.common.concept.Parenthood
import com.vaticle.typedb.iam.simulation.common.concept.Person
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.CREATE
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.MATCH
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.RETURN
import com.vaticle.typedb.iam.simulation.neo4j.Literals.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.BORN_IN
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CHILD_EMAIL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CITY_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CODE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CONTAINED_IN
import com.vaticle.typedb.iam.simulation.neo4j.Literals.COUNTRY
import com.vaticle.typedb.iam.simulation.neo4j.Literals.COUNTRY_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.EMAIL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.FATHER_EMAIL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.MARRIAGE_DATE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.MARRIAGE_LICENCE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.MARRIED_TO
import com.vaticle.typedb.iam.simulation.neo4j.Literals.MOTHER_EMAIL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PARENT_OF
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PERSON
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PERSON_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.RESIDES_IN
import com.vaticle.typedb.iam.simulation.agent.ParenthoodAgent
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typedb.simulation.neo4j.Neo4jClient
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import org.neo4j.driver.Session
import org.neo4j.driver.Transaction
import java.time.LocalDateTime
import java.util.Comparator
import java.util.stream.Collectors
import java.util.stream.Stream

class Neo4jParenthoodAgent(client: Neo4jClient, context: Context) : ParenthoodAgent<Session>(client, context) {

    override fun run(session: Session, partition: Country, random: RandomSource): List<Report> {
        val reports: MutableList<Report> = ArrayList()
        session.writeTransaction { tx ->
            val marriageDate = context.today().minusYears(context.model.yearsBeforeParenthood.toLong())
            val marriages = matchMarriages(tx, partition, marriageDate).sorted(Comparator.comparing { it.licence }).collect(
                Collectors.toList()
            )
            val newBorns = matchNewborns(tx, partition, context.today()).sorted(Comparator.comparing { it.email }).collect(
                Collectors.toList()
            )
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

    private fun matchNewborns(tx: Transaction, country: Country, today: LocalDateTime): Stream<Person> {
        val query = "$MATCH ($PERSON:$PERSON_LABEL {$BIRTH_DATE: \$$BIRTH_DATE})" +
                "-[:$BORN_IN]->(:$CITY_LABEL)-[:$CONTAINED_IN]->($COUNTRY:$COUNTRY_LABEL {$CODE: \$$CODE}) \n" +
                "$RETURN $PERSON.$EMAIL"
        val parameters = mapOf(CODE to country.code, BIRTH_DATE to today)
        return tx.run(Query(query, parameters)).stream()
            .map { record: Record -> Person(email = record.asMap()["$PERSON.$EMAIL"] as String) }
    }

    private fun matchMarriages(tx: Transaction, country: Country, marriageDate: LocalDateTime): Stream<Marriage> {
        val query = "$MATCH ($W:$PERSON_LABEL)-[:$RESIDES_IN]->(:$CITY_LABEL)-[:$CONTAINED_IN]->($COUNTRY:$COUNTRY_LABEL {$CODE: \$$CODE}),\n" +
                "($W)-[$M:$MARRIED_TO {$MARRIAGE_DATE: \$$MARRIAGE_DATE}]->($H:$PERSON_LABEL)" +
                "$RETURN $W.$EMAIL, $H.$EMAIL, $M.$MARRIAGE_LICENCE, $M.$MARRIAGE_DATE"
        val parameters = mapOf(MARRIAGE_DATE to marriageDate, CODE to country.code)
        tx.run(Query(query, parameters))
        return tx.run(Query(query, parameters)).stream().map { record: Record ->
            Marriage(
                wife = Person(email = record.asMap()["$W.$EMAIL"] as String),
                husband = Person(email = record.asMap()["$H.$EMAIL"] as String),
                licence = record.asMap()["$M.$MARRIAGE_LICENCE"] as String,
                date = record.asMap()["$M.$MARRIAGE_DATE"] as LocalDateTime
            )
        }
    }

    private fun insertParenthood(
        tx: Transaction, motherEmail: String, fatherEmail: String, childEmail: String
    ): Parenthood? {
        val query = "$MATCH " +
                "($M:$PERSON_LABEL {$EMAIL: \$$MOTHER_EMAIL}),\n" +
                "($F:$PERSON_LABEL {$EMAIL: \$$FATHER_EMAIL}),\n" +
                "($C:$PERSON_LABEL {$EMAIL: \$$CHILD_EMAIL})\n" +
                "$CREATE ($M)-[:$PARENT_OF]->($C),\n" +
                "($F)-[:$PARENT_OF]->($C)"
        val parameters = mapOf(MOTHER_EMAIL to motherEmail, FATHER_EMAIL to fatherEmail, CHILD_EMAIL to childEmail)
        tx.run(Query(query, parameters))
        return if (context.isReporting) report(tx, motherEmail, fatherEmail, childEmail) else null
    }

    private fun report(tx: Transaction, motherEmail: String, fatherEmail: String, childEmail: String): Parenthood {
        val query = "$MATCH " +
                "($M:$PERSON_LABEL {$EMAIL: \$$MOTHER_EMAIL}),\n" +
                "($F:$PERSON_LABEL {$EMAIL: \$$FATHER_EMAIL}),\n" +
                "($C:$PERSON_LABEL {$EMAIL: \$$CHILD_EMAIL}),\n" +
                "($M)-[:$PARENT_OF]->($C),\n" +
                "($F)-[:$PARENT_OF]->($C)\n" +
                "$RETURN $M.$EMAIL, $F.$EMAIL, $C.$EMAIL"
        val parameters = mapOf(MOTHER_EMAIL to motherEmail, FATHER_EMAIL to fatherEmail, CHILD_EMAIL to childEmail)
        val answers = tx.run(Query(query, parameters)).list()
        assert(answers.size == 1)
        val inserted = answers[0].asMap()
        val mother = Person(email = inserted["$M.$EMAIL"] as String)
        val father = Person(email = inserted["$F.$EMAIL"] as String)
        val child = Person(email = inserted["$C.$EMAIL"] as String)
        return Parenthood(mother, father, child)
    }

    companion object {
        private const val C = "c"
        private const val F = "f"
        private const val H = "h"
        private const val M = "m"
        private const val W = "w"
    }
}
