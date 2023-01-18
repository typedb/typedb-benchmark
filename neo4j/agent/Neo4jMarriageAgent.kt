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
import com.vaticle.typedb.benchmark.neo4j.Labels
import com.vaticle.typedb.benchmark.neo4j.Labels.BIRTH_DATE
import com.vaticle.typedb.benchmark.neo4j.Labels.CODE
import com.vaticle.typedb.benchmark.neo4j.Labels.GENDER
import com.vaticle.typedb.benchmark.neo4j.Labels.MARRIAGE_DATE
import com.vaticle.typedb.benchmark.neo4j.Labels.MARRIAGE_LICENCE
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent
import com.vaticle.typedb.benchmark.simulation.driver.Client
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Stream

class Neo4jMarriageAgent(client: Neo4jClient, context: Context) : MarriageAgent<Neo4jTransaction>(client, context) {
    override fun matchPartner(
        tx: Neo4jTransaction, country: Country, birthDate: LocalDateTime, gender: Gender
    ): Stream<Person> {
        val query = "MATCH (person:Person {birthDate: \$birthDate, gender: \$gender})" +
                "-[:RESIDES_IN]->(city:City)-[:CONTAINED_IN]->(country:Country {code: \$code}) \n" +
                "RETURN person.email"
        val parameters = mapOf(CODE to country.code, BIRTH_DATE to birthDate, GENDER to gender.value)
        return tx.execute(Query(query, parameters)).stream()
            .map { record: Record -> Person((record.asMap()["person.email"] as String?)!!) }
    }

    override fun insertMarriage(
        tx: Neo4jTransaction, wifeEmail: String, husbandEmail: String,
        marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage? {
        val query = "MATCH " +
                "(x:Person {email: \$wifeEmail}), \n" +
                "(y:Person {email: \$husbandEmail}) \n" +
                "CREATE (x)-[:MARRIED_TO {marriageLicence: \$marriageLicence, marriageDate: \$marriageDate}]->(y)"
        val parameters = mapOf(
            "wifeEmail" to wifeEmail,
            "husbandEmail" to husbandEmail,
            MARRIAGE_LICENCE to marriageLicence,
            MARRIAGE_DATE to marriageDate
        )
        tx.execute(Query(query, parameters))
        return if (context.isReporting) report(tx, wifeEmail, husbandEmail, marriageLicence, marriageDate) else null
    }

    private fun report(
        tx: Neo4jTransaction, wifeEmail: String, husbandEmail: String,
        marriageLicence: String, marriageDate: LocalDateTime
    ): Marriage {
        val query = "MATCH " +
                "(x:Person {email: \$wifeEmail}), \n" +
                "(y:Person {email: \$husbandEmail}), \n" +
                "(x)-[m:MARRIED_TO {marriageLicence: \$marriageLicence, marriageDate: \$marriageDate}]->(y) \n" +
                "RETURN x.email, y.email, m.marriageLicence, m.marriageDate"
        val parameters = mapOf(
            "wifeEmail" to wifeEmail,
            "husbandEmail" to husbandEmail,
            MARRIAGE_LICENCE to marriageLicence,
            MARRIAGE_DATE to marriageDate
        )
        val answers = tx.execute(Query(query, parameters))
        assert(answers.size == 1)
        val inserted = answers[0].asMap()
        val person1 = Person((inserted[X + "." + Labels.EMAIL] as String?)!!)
        val person2 = Person((inserted[Y + "." + Labels.EMAIL] as String?)!!)
        return Marriage(
            person1, person2, (inserted["$M.$MARRIAGE_LICENCE"] as String?)!!,
            (inserted["$M.$MARRIAGE_DATE"] as LocalDateTime?)!!
        )
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
        private const val M = "m"
    }
}
