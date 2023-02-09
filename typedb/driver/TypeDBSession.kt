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
package com.vaticle.typedb.simulation.typedb.driver

import com.vaticle.typedb.simulation.common.driver.Session
import com.vaticle.typedb.client.api.TypeDBOptions
import com.vaticle.typedb.client.api.TypeDBSession
import com.vaticle.typedb.client.api.TypeDBTransaction.Type.READ
import com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE

class TypeDBSession(private val nativeSession: TypeDBSession) : Session<TypeDBTransaction> {
    override fun writeTransaction(): TypeDBTransaction {
        return TypeDBTransaction(nativeSession.transaction(WRITE))
    }

    override fun readTransaction(): TypeDBTransaction {
        return TypeDBTransaction(nativeSession.transaction(READ))
    }

    override fun reasoningTransaction(): TypeDBTransaction {
        return TypeDBTransaction(nativeSession.transaction(READ, TypeDBOptions.core().infer(true)))
    }

    override fun close() {
        nativeSession.close()
    }
}
