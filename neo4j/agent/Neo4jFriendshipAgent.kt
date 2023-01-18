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
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.neo4j.Labels
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent
import com.vaticle.typedb.common.collection.Collections
import com.vaticle.typedb.common.collection.Pair
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Stream

class Neo4jFriendshipAgent(client: Neo4jClient, context: Context) : FriendshipAgent<Neo4jTransaction>(client, context) {
    override fun matchTeenagers(tx: Neo4jTransaction, country: Country, birthDate: LocalDateTime): Stream<Person> {
        val query = "MATCH (person:Person {birthDate: \$birthDate})" +
                "-[:RESIDES_IN]->(city:City)-[:CONTAINED_IN]->(country:Country {code: \$code}) \n" +
                "RETURN person.email"
        val parameters = mapOf(Labels.CODE to country.code, Labels.BIRTH_DATE to birthDate)
        return tx.execute(Query(query, parameters)).stream()
            .map { record: Record -> Person((record.asMap()["person.email"] as String?)!!) }
    }

    override fun insertFriends(tx: Neo4jTransaction, email1: String, email2: String): Pair<Person, Person>? {
        val query = "MATCH " +
                "(x:Person {email: \$email1}), \n" +
                "(y:Person {email: \$email2}) \n" +
                "CREATE (x)-[:FRIENDS_WITH]->(y)"
        val parameters = mapOf("email1" to email1, "email2" to email2)
        tx.execute(Query(query, parameters))
        return if (context.isReporting) report(tx, email1, email2) else null
    }

    private fun report(tx: Neo4jTransaction, email1: String, email2: String): Pair<Person, Person> {
        val query = "MATCH " +
                "(x:Person {email: \$email1}), \n" +
                "(y:Person {email: \$email2}), \n" +
                "(x)-[:FRIENDS_WITH]->(y) \n" +
                "RETURN x.email, y.email"
        val parameters = mapOf("email1" to email1, "email2" to email2)
        val answers = tx.execute(Query(query, parameters))
        assert(answers.size == 1)
        val inserted = answers[0].asMap()
        val person1 = Person((inserted[X + "." + Labels.EMAIL] as String?)!!)
        val person2 = Person((inserted[Y + "." + Labels.EMAIL] as String?)!!)
        return Pair(person1, person2)
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
    }
}
