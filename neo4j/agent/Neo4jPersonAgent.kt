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

import com.vaticle.typedb.benchmark.common.concept.City
import com.vaticle.typedb.benchmark.common.concept.Gender
import com.vaticle.typedb.benchmark.common.concept.Person
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.neo4j.Labels.ADDRESS
import com.vaticle.typedb.benchmark.neo4j.Labels.BIRTH_DATE
import com.vaticle.typedb.benchmark.neo4j.Labels.CITY
import com.vaticle.typedb.benchmark.neo4j.Labels.CODE
import com.vaticle.typedb.benchmark.neo4j.Labels.EMAIL
import com.vaticle.typedb.benchmark.neo4j.Labels.FIRST_NAME
import com.vaticle.typedb.benchmark.neo4j.Labels.GENDER
import com.vaticle.typedb.benchmark.neo4j.Labels.LAST_NAME
import com.vaticle.typedb.benchmark.neo4j.Labels.PERSON
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction
import com.vaticle.typedb.benchmark.simulation.agent.PersonAgent
import com.vaticle.typedb.common.collection.Pair
import org.neo4j.driver.Query
import java.time.LocalDateTime

class Neo4jPersonAgent(client: Neo4jClient, context: Context) : PersonAgent<Neo4jTransaction>(client, context) {
    override fun insertPerson(
        tx: Neo4jTransaction, email: String, firstName: String, lastName: String,
        address: String, gender: Gender, birthDate: LocalDateTime, city: City
    ): Pair<Person, City.Report>? {
        val query = "MATCH (c:City {code: \$code}) " +
                "CREATE (person:Person {" +
                "email: \$email, " +
                "firstName: \$firstName, " +
                "lastName: \$lastName, " +
                "address: \$address, " +
                "gender: \$gender, " +
                "birthDate: \$birthDate" +
                "})-[:BORN_IN]->(c), " +
                "(person)-[:RESIDES_IN]->(c)"
        val parameters = mapOf(
            CODE to city.code,
            EMAIL to email,
            FIRST_NAME to firstName,
            LAST_NAME to lastName,
            ADDRESS to address,
            GENDER to gender.value,
            BIRTH_DATE to birthDate
        )
        tx.execute(Query(query, parameters))
        return if (context.isReporting) report(tx, email) else null
    }

    private fun report(tx: Neo4jTransaction, email: String): Pair<Person, City.Report> {
        val answers = tx.execute(
            Query(
                "MATCH (person:Person {email: '$email'})-[:BORN_IN]->(city:City), " +
                        "(person)-[:RESIDES_IN]->(city) " +
                        "RETURN person.email, person.firstName, person.lastName, person.address, " +
                        "person.gender, person.birthDate, city.code"
            )
        )
        assert(answers.size == 1)
        val inserted = answers[0].asMap()
        val person = Person(
            email = inserted["$PERSON.$EMAIL"] as String,
            firstName = inserted["$PERSON.$FIRST_NAME"] as String,
            lastName = inserted["$PERSON.$LAST_NAME"] as String,
            address = inserted["$PERSON.$ADDRESS"] as String,
            gender = Gender.of(inserted["$PERSON.$GENDER"] as String),
            birthDate = inserted["$PERSON.$BIRTH_DATE"] as LocalDateTime
        )
        val city = City.Report(code = inserted["$CITY.$CODE"] as String)
        return Pair(person, city)
    }
}
