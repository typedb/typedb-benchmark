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
import com.vaticle.typedb.benchmark.typedb.Labels
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typeql.lang.TypeQL
import java.time.LocalDateTime
import java.util.stream.Collectors.toList

class TypeDBLineageAgent(client: TypeDBClient, context: Context) : LineageAgent<TypeDBTransaction>(client, context) {
    override fun matchLineages(tx: TypeDBTransaction, country: Country, startDay: LocalDateTime, today: LocalDateTime?) {
        tx.query().match(
            TypeQL.match(
                TypeQL.rel(Labels.CONTAINER, Labels.COUNTRY).rel(Labels.CONTAINED, Labels.CITY).isa(Labels.CONTAINS),
                TypeQL.`var`(Labels.COUNTRY).isa(Labels.COUNTRY).has(Labels.CODE, country.code),
                TypeQL.`var`(Labels.CITY).isa(Labels.CITY),
                TypeQL.`var`(Labels.ANCESTOR).isa(Labels.PERSON).has(Labels.BIRTH_DATE, startDay),
                TypeQL.`var`().rel(Labels.PLACE, TypeQL.`var`(Labels.CITY))
                    .rel(Labels.CHILD, TypeQL.`var`(Labels.ANCESTOR))
                    .isa(Labels.BIRTH_PLACE),
                TypeQL.`var`(Labels.DESCENDENT).isa(Labels.PERSON).has(Labels.BIRTH_DATE, today),
                TypeQL.rel(Labels.ANCESTOR, Labels.ANCESTOR).rel(Labels.DESCENDENT, Labels.DESCENDENT)
                    .isa(Labels.LINEAGE)
            )
        ).collect(toList())
    }
}
