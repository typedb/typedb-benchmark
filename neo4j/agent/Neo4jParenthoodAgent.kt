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
import com.vaticle.typedb.benchmark.neo4j.Labels
import com.vaticle.typedb.benchmark.neo4j.Labels.BIRTH_DATE
import com.vaticle.typedb.benchmark.neo4j.Labels.CODE
import com.vaticle.typedb.benchmark.neo4j.Labels.MARRIAGE_DATE
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction
import com.vaticle.typedb.benchmark.simulation.agent.ParenthoodAgent
import com.vaticle.typedb.benchmark.simulation.driver.Client
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Stream

class Neo4jParenthoodAgent(client: Neo4jClient, context: Context) : ParenthoodAgent<Neo4jTransaction>(client, context) {
    override fun matchNewborns(tx: Neo4jTransaction, country: Country, today: LocalDateTime): Stream<Person> {
        val query = "MATCH (person:Person {birthDate: \$birthDate})" +
                "-[:BORN_IN]->(:City)-[:CONTAINED_IN]->(country:Country {code: \$code}) \n" +
                "RETURN person.email"
        val parameters = mapOf(CODE to country.code, BIRTH_DATE to today)
        return tx.execute(Query(query, parameters)).stream()
            .map { record: Record -> Person((record.asMap()["person.email"] as String?)!!) }
    }

    override fun matchMarriages(tx: Neo4jTransaction, country: Country, marriageDate: LocalDateTime): Stream<Marriage> {
        val query = "MATCH (w:Person)-[:RESIDES_IN]->(:City)-[:CONTAINED_IN]->(country:Country {code: \$code}),\n" +
                "(w)-[m:MARRIED_TO {marriageDate: \$marriageDate}]->(h:Person)" +
                "RETURN w.email, h.email, m.marriageLicence";
        val parameters = mapOf(MARRIAGE_DATE to marriageDate, CODE to country.code)
        tx.execute(Query(query, parameters))
        return tx.execute(Query(query, parameters)).stream().map { record: Record ->
            Marriage(
                Person((record.asMap()["w.email"] as String?)!!),
                Person((record.asMap()["h.email"] as String?)!!),
                (record.asMap()["m.marriageLicence"] as String?)!!,
                (record.asMap()["m.marriageDate"] as LocalDateTime?)!!
            )
        }
    }

    override fun insertParenthood(
        tx: Neo4jTransaction, motherEmail: String, fatherEmail: String,
        childEmail: String
    ): Parenthood? {
        val query = "MATCH " +
                "(m:Person {email: \$motherEmail}),\n" +
                "(f:Person {email: \$fatherEmail}),\n" +
                "(c:Person {email: \$childEmail})\n" +
                "CREATE (m)-[:PARENT_OF]->(c),\n" +
                "(f)-[:PARENT_OF]->(c)"
        val parameters = mapOf("motherEmail" to motherEmail, "fatherEmail" to fatherEmail, "childEmail" to childEmail)
        tx.execute(Query(query, parameters))
        return if (context.isReporting) report(tx, motherEmail, fatherEmail, childEmail) else null
    }

    private fun report(tx: Neo4jTransaction, motherEmail: String, fatherEmail: String, childEmail: String): Parenthood {
        val query = "MATCH " +
                "(m:Person {email: \$motherEmail}),\n" +
                "(f:Person {email: \$fatherEmail}),\n" +
                "(c:Person {email: \$childEmail}),\n" +
                "(m)-[:PARENT_OF]->(c),\n" +
                "(f)-[:PARENT_OF]->(c)\n" +
                "RETURN m.email, f.email, c.email"
        val parameters = mapOf("motherEmail" to motherEmail, "fatherEmail" to fatherEmail, "childEmail" to childEmail)
        val answers = tx.execute(Query(query, parameters))
        assert(answers.size == 1)
        val inserted = answers[0].asMap()
        val mother = Person((inserted[M + "." + Labels.EMAIL] as String?)!!)
        val father = Person((inserted[F + "." + Labels.EMAIL] as String?)!!)
        val child = Person((inserted[C + "." + Labels.EMAIL] as String?)!!)
        return Parenthood(mother, father, child)
    }

    companion object {
        private const val M = "m"
        private const val F = "f"
        private const val C = "c"
    }
}
