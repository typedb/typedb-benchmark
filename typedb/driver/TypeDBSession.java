/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.benchmark.typedb.driver;

import com.vaticle.typedb.benchmark.simulation.driver.Session;

import static com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE;

public class TypeDBSession implements Session<TypeDBTransaction> {

    private final com.vaticle.typedb.client.api.TypeDBSession nativeSession;

    public TypeDBSession(com.vaticle.typedb.client.api.TypeDBSession nativeSession) {
        this.nativeSession = nativeSession;
    }

    @Override
    public TypeDBTransaction transaction() {
        return new TypeDBTransaction(nativeSession.transaction(WRITE));
    }

    @Override
    public void close() {
        nativeSession.close();
    }
}
