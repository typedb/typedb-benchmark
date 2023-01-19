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
import com.vaticle.typedb.benchmark.common.concept.Gender
import com.vaticle.typedb.benchmark.common.concept.Marriage
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.neo4j.Keywords.CREATE
import com.vaticle.typedb.benchmark.neo4j.Keywords.MATCH
import com.vaticle.typedb.benchmark.neo4j.Keywords.RETURN
import com.vaticle.typedb.benchmark.neo4j.Literals.BIRTH_DATE
import com.vaticle.typedb.benchmark.neo4j.Literals.CITY
import com.vaticle.typedb.benchmark.neo4j.Literals.CITY_LABEL
import com.vaticle.typedb.benchmark.neo4j.Literals.CODE
import com.vaticle.typedb.benchmark.neo4j.Literals.CONTAINED_IN
import com.vaticle.typedb.benchmark.neo4j.Literals.COUNTRY
import com.vaticle.typedb.benchmark.neo4j.Literals.COUNTRY_LABEL
import com.vaticle.typedb.benchmark.neo4j.Literals.EMAIL
import com.vaticle.typedb.benchmark.neo4j.Literals.GENDER
import com.vaticle.typedb.benchmark.neo4j.Literals.HUSBAND_EMAIL
import com.vaticle.typedb.benchmark.neo4j.Literals.MARRIAGE_DATE
import com.vaticle.typedb.benchmark.neo4j.Literals.MARRIAGE_LICENCE
import com.vaticle.typedb.benchmark.neo4j.Literals.MARRIED_TO
import com.vaticle.typedb.benchmark.neo4j.Literals.PERSON
import com.vaticle.typedb.benchmark.neo4j.Literals.PERSON_LABEL
import com.vaticle.typedb.benchmark.neo4j.Literals.RESIDES_IN
import com.vaticle.typedb.benchmark.neo4j.Literals.WIFE_EMAIL
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import java.time.LocalDateTime
import java.util.stream.Stream

class Neo4jMarriageAgent(client: Neo4jClient, context: Context) : MarriageAgent<Neo4jTransaction>(client, context) {
    override fun matchPartner(
        tx: Neo4jTransaction, country: Country, birthDate: LocalDateTime, gender: Gender
    ): Stream<Person> {
        val query = "$MATCH ($PERSON:$PERSON_LABEL {$BIRTH_DATE: \$$BIRTH_DATE, $GENDER: \$$GENDER})" +
                "-[:$RESIDES_IN]->($CITY:$CITY_LABEL)-[:$CONTAINED_IN]->($COUNTRY:$COUNTRY_LABEL {$CODE: \$$CODE}) \n" +
                "$RETURN $PERSON.$EMAIL"
        val parameters = mapOf(CODE to country.code, BIRTH_DATE to birthDate, GENDER to gender.value)
        return tx.execute(Query(query, parameters)).stream()
            .map { record: Record -> Person(email = record.asMap()["$PERSON.$EMAIL"] as String) }
    }

    override fun insertMarriage(
        tx: Neo4jTransaction, wifeEmail: String, husbandEmail: String,
        marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage? {
        val query = "$MATCH " +
                "($X:$PERSON_LABEL {$EMAIL: \$$WIFE_EMAIL}), \n" +
                "($Y:$PERSON_LABEL {$EMAIL: \$$HUSBAND_EMAIL}) \n" +
                "$CREATE (x)-[:$MARRIED_TO {$MARRIAGE_LICENCE: \$$MARRIAGE_LICENCE, $MARRIAGE_DATE: \$$MARRIAGE_DATE}]->($Y)"
        val parameters = mapOf(
            WIFE_EMAIL to wifeEmail, HUSBAND_EMAIL to husbandEmail,
            MARRIAGE_LICENCE to marriageLicence, MARRIAGE_DATE to marriageDate
        )
        tx.execute(Query(query, parameters))
        return if (context.isReporting) report(tx, wifeEmail, husbandEmail, marriageLicence, marriageDate) else null
    }

    private fun report(
        tx: Neo4jTransaction, wifeEmail: String, husbandEmail: String,
        marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage {
        val query = "$MATCH " +
                "($X:$PERSON_LABEL {$EMAIL: \$$WIFE_EMAIL}), \n" +
                "($Y:$PERSON_LABEL {$EMAIL: \$$HUSBAND_EMAIL}), \n" +
                "($X)-[$M:$MARRIED_TO {$MARRIAGE_LICENCE: \$$MARRIAGE_LICENCE, $MARRIAGE_DATE: \$$MARRIAGE_DATE}]->($Y) \n" +
                "$RETURN $X.$EMAIL, $Y.$EMAIL, $M.$MARRIAGE_LICENCE, $M.$MARRIAGE_DATE"
        val parameters = mapOf(
            WIFE_EMAIL to wifeEmail, HUSBAND_EMAIL to husbandEmail,
            MARRIAGE_LICENCE to marriageLicence, MARRIAGE_DATE to marriageDate
        )
        val answers = tx.execute(Query(query, parameters))
        assert(answers.size == 1)
        val inserted = answers[0].asMap()
        val person1 = Person(email = inserted["$X.$EMAIL"] as String)
        val person2 = Person(email = inserted["$Y.$EMAIL"] as String)
        return Marriage(
            wife = person1, husband = person2, licence = inserted["$M.$MARRIAGE_LICENCE"] as String,
            date = inserted["$M.$MARRIAGE_DATE"] as LocalDateTime
        )
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
        private const val M = "m"
    }
}
