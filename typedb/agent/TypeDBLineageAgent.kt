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
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.simulation.agent.LineageAgent
import com.vaticle.typedb.benchmark.typedb.Labels.ANCESTOR
import com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE
import com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_PLACE
import com.vaticle.typedb.benchmark.typedb.Labels.CHILD
import com.vaticle.typedb.benchmark.typedb.Labels.CITY
import com.vaticle.typedb.benchmark.typedb.Labels.CODE
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS
import com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY
import com.vaticle.typedb.benchmark.typedb.Labels.DESCENDENT
import com.vaticle.typedb.benchmark.typedb.Labels.LINEAGE
import com.vaticle.typedb.benchmark.typedb.Labels.PERSON
import com.vaticle.typedb.benchmark.typedb.Labels.PLACE
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBLineageAgent(client: TypeDBClient, context: Context) : LineageAgent<TypeDBTransaction>(client, context) {
    override fun matchLineages(tx: TypeDBTransaction, country: Country, startDay: LocalDateTime, today: LocalDateTime) {
        tx.query().match(match(
            rel(CONTAINER, COUNTRY).rel(CONTAINED, CITY).isa(CONTAINS),
            `var`(COUNTRY).isa(COUNTRY).has(CODE, country.code),
            `var`(CITY).isa(CITY),
            `var`(ANCESTOR).isa(PERSON).has(BIRTH_DATE, startDay),
            `var`().rel(PLACE, `var`(CITY)).rel(CHILD, `var`(ANCESTOR)).isa(BIRTH_PLACE),
            `var`(DESCENDENT).isa(PERSON).has(BIRTH_DATE, today),
            rel(ANCESTOR, ANCESTOR).rel(DESCENDENT, DESCENDENT).isa(LINEAGE)
        )).collect(toList())
    }
}
