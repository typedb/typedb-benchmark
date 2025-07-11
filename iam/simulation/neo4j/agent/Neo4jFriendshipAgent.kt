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
import com.vaticle.typedb.iam.simulation.common.concept.Person
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.CREATE
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.MATCH
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.RETURN
import com.vaticle.typedb.iam.simulation.neo4j.Literals.BIRTH_DATE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CITY
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CITY_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CODE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CONTAINED_IN
import com.vaticle.typedb.iam.simulation.neo4j.Literals.COUNTRY
import com.vaticle.typedb.iam.simulation.neo4j.Literals.COUNTRY_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.EMAIL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.EMAIL1
import com.vaticle.typedb.iam.simulation.neo4j.Literals.EMAIL2
import com.vaticle.typedb.iam.simulation.neo4j.Literals.FRIENDS_WITH
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PERSON
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PERSON_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.RESIDES_IN
import com.vaticle.typedb.iam.simulation.agent.FriendshipAgent
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typedb.simulation.neo4j.Neo4jClient
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import org.neo4j.driver.Session
import org.neo4j.driver.Transaction
import java.time.LocalDateTime
import java.util.Comparator
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class Neo4jFriendshipAgent(client: Neo4jClient, context: Context) : FriendshipAgent<Session>(client, context) {

    override fun run(session: Session, partition: Country, random: RandomSource): List<Report> {
        val reports = mutableListOf<Report>()
        session.writeTransaction { tx ->
            val birthDate = context.today().minusYears(context.model.ageOfFriendship.toLong())
            val teenagers = matchTeenagers(tx, partition, birthDate).sorted(Comparator.comparing { it.email }).collect(toList())
            random.randomPairs(teenagers, log2(context.model.populationGrowth).coerceAtMost(1)).forEach { friends ->
                val inserted = insertFriends(tx, friends.first.email, friends.second.email)
                if (context.isReporting) {
                    requireNotNull(inserted)
                    reports.add(Report(
                        input = listOf(friends.first.email, friends.second.email),
                        output = listOf(inserted.first, inserted.second)
                    ))
                } else assert(inserted == null)
            }
            tx.commit()
        }
        return reports
    }

    private fun matchTeenagers(tx: Transaction, country: Country, birthDate: LocalDateTime): Stream<Person> {
        val query = "$MATCH ($PERSON:$PERSON_LABEL {$BIRTH_DATE: \$$BIRTH_DATE})" +
                "-[:$RESIDES_IN]->($CITY:$CITY_LABEL)-[:$CONTAINED_IN]->($COUNTRY:$COUNTRY_LABEL {$CODE: \$$CODE}) \n" +
                "$RETURN $PERSON.$EMAIL"
        val parameters = mapOf(CODE to country.code, BIRTH_DATE to birthDate)
        return tx.run(Query(query, parameters)).stream()
            .map { record: Record -> Person(email = record.asMap()["person.$EMAIL"] as String) }
    }

    private fun insertFriends(tx: Transaction, email1: String, email2: String): Pair<Person, Person>? {
        val query = "$MATCH " +
                "($X:$PERSON_LABEL {$EMAIL: \$$EMAIL1}), \n" +
                "($Y:$PERSON_LABEL {$EMAIL: \$$EMAIL2}) \n" +
                "$CREATE ($X)-[:$FRIENDS_WITH]->($Y)"
        val parameters = mapOf(EMAIL1 to email1, EMAIL2 to email2)
        tx.run(Query(query, parameters))
        return if (context.isReporting) report(tx, email1, email2) else null
    }

    private fun report(tx: Transaction, email1: String, email2: String): Pair<Person, Person> {
        val query = "$MATCH " +
                "($X:$PERSON_LABEL {$EMAIL: \$$EMAIL1}), \n" +
                "($Y:$PERSON_LABEL {$EMAIL: \$$EMAIL2}), \n" +
                "($X)-[:$FRIENDS_WITH]->($Y) \n" +
                "$RETURN $X.$EMAIL, $Y.$EMAIL"
        val parameters = mapOf(EMAIL1 to email1, EMAIL2 to email2)
        val answers = tx.run(Query(query, parameters)).list()
        assert(answers.size == 1)
        val inserted = answers[0].asMap()
        val person1 = Person(email = inserted["$X.$EMAIL"] as String)
        val person2 = Person(email = inserted["$Y.$EMAIL"] as String)
        return Pair(person1, person2)
    }

    companion object {
        private const val X = "x"
        private const val Y = "y"
    }
}
