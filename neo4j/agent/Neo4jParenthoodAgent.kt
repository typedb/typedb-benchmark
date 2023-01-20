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
package com.vaticle.typedb.benchmark.neo4j.agent

import com.vaticle.typedb.benchmark.common.concept.Country
import com.vaticle.typedb.benchmark.common.concept.Marriage
import com.vaticle.typedb.benchmark.common.concept.Parenthood
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.neo4j.Keywords.CREATE
import com.vaticle.typedb.benchmark.neo4j.Keywords.MATCH
import com.vaticle.typedb.benchmark.neo4j.Keywords.RETURN
import com.vaticle.typedb.benchmark.neo4j.Literals.BIRTH_DATE
import com.vaticle.typedb.benchmark.neo4j.Literals.BORN_IN
import com.vaticle.typedb.benchmark.neo4j.Literals.CHILD_EMAIL
import com.vaticle.typedb.benchmark.neo4j.Literals.CITY_LABEL
import com.vaticle.typedb.benchmark.neo4j.Literals.CODE
import com.vaticle.typedb.benchmark.neo4j.Literals.CONTAINED_IN
import com.vaticle.typedb.benchmark.neo4j.Literals.COUNTRY
import com.vaticle.typedb.benchmark.neo4j.Literals.COUNTRY_LABEL
import com.vaticle.typedb.benchmark.neo4j.Literals.EMAIL
import com.vaticle.typedb.benchmark.neo4j.Literals.FATHER_EMAIL
import com.vaticle.typedb.benchmark.neo4j.Literals.MARRIAGE_DATE
import com.vaticle.typedb.benchmark.neo4j.Literals.MARRIAGE_LICENCE
import com.vaticle.typedb.benchmark.neo4j.Literals.MARRIED_TO
import com.vaticle.typedb.benchmark.neo4j.Literals.MOTHER_EMAIL
import com.vaticle.typedb.benchmark.neo4j.Literals.PARENT_OF
import com.vaticle.typedb.benchmark.neo4j.Literals.PERSON
import com.vaticle.typedb.benchmark.neo4j.Literals.PERSON_LABEL
import com.vaticle.typedb.benchmark.neo4j.Literals.RESIDES_IN
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction
import com.vaticle.typedb.benchmark.simulation.agent.ParenthoodAgent
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import java.time.LocalDateTime
import java.util.stream.Stream

class Neo4jParenthoodAgent(client: Neo4jClient, context: Context) : ParenthoodAgent<Neo4jTransaction>(client, context) {
    override fun matchNewborns(tx: Neo4jTransaction, country: Country, today: LocalDateTime): Stream<Person> {
        val query = "$MATCH ($PERSON:$PERSON_LABEL {$BIRTH_DATE: \$$BIRTH_DATE})" +
                "-[:$BORN_IN]->(:$CITY_LABEL)-[:$CONTAINED_IN]->($COUNTRY:$COUNTRY_LABEL {$CODE: \$$CODE}) \n" +
                "$RETURN $PERSON.$EMAIL"
        val parameters = mapOf(CODE to country.code, BIRTH_DATE to today)
        return tx.execute(Query(query, parameters)).stream()
            .map { record: Record -> Person(email = record.asMap()["$PERSON.$EMAIL"] as String) }
    }

    override fun matchMarriages(tx: Neo4jTransaction, country: Country, marriageDate: LocalDateTime): Stream<Marriage> {
        val query = "$MATCH ($W:$PERSON_LABEL)-[:$RESIDES_IN]->(:$CITY_LABEL)-[:$CONTAINED_IN]->($COUNTRY:$COUNTRY_LABEL {$CODE: \$$CODE}),\n" +
                "($W)-[$M:$MARRIED_TO {$MARRIAGE_DATE: \$$MARRIAGE_DATE}]->($H:$PERSON_LABEL)" +
                "$RETURN $W.$EMAIL, $H.$EMAIL, $M.$MARRIAGE_LICENCE, $M.$MARRIAGE_DATE"
        val parameters = mapOf(MARRIAGE_DATE to marriageDate, CODE to country.code)
        tx.execute(Query(query, parameters))
        return tx.execute(Query(query, parameters)).stream().map { record: Record ->
            Marriage(
                wife = Person(email = record.asMap()["$W.$EMAIL"] as String),
                husband = Person(email = record.asMap()["$H.$EMAIL"] as String),
                licence = record.asMap()["$M.$MARRIAGE_LICENCE"] as String,
                date = record.asMap()["$M.$MARRIAGE_DATE"] as LocalDateTime
            )
        }
    }

    override fun insertParenthood(
        tx: Neo4jTransaction, motherEmail: String, fatherEmail: String, childEmail: String
    ): Parenthood? {
        val query = "$MATCH " +
                "($M:$PERSON_LABEL {$EMAIL: \$$MOTHER_EMAIL}),\n" +
                "($F:$PERSON_LABEL {$EMAIL: \$$FATHER_EMAIL}),\n" +
                "($C:$PERSON_LABEL {$EMAIL: \$$CHILD_EMAIL})\n" +
                "$CREATE ($M)-[:$PARENT_OF]->($C),\n" +
                "($F)-[:$PARENT_OF]->($C)"
        val parameters = mapOf(MOTHER_EMAIL to motherEmail, FATHER_EMAIL to fatherEmail, CHILD_EMAIL to childEmail)
        tx.execute(Query(query, parameters))
        return if (context.isReporting) report(tx, motherEmail, fatherEmail, childEmail) else null
    }

    private fun report(tx: Neo4jTransaction, motherEmail: String, fatherEmail: String, childEmail: String): Parenthood {
        val query = "$MATCH " +
                "($M:$PERSON_LABEL {$EMAIL: \$$MOTHER_EMAIL}),\n" +
                "($F:$PERSON_LABEL {$EMAIL: \$$FATHER_EMAIL}),\n" +
                "($C:$PERSON_LABEL {$EMAIL: \$$CHILD_EMAIL}),\n" +
                "($M)-[:$PARENT_OF]->($C),\n" +
                "($F)-[:$PARENT_OF]->($C)\n" +
                "$RETURN $M.$EMAIL, $F.$EMAIL, $C.$EMAIL"
        val parameters = mapOf(MOTHER_EMAIL to motherEmail, FATHER_EMAIL to fatherEmail, CHILD_EMAIL to childEmail)
        val answers = tx.execute(Query(query, parameters))
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
