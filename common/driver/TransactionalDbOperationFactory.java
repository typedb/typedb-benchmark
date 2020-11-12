/*
 * Copyright (C) 2020 Grakn Labs
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

package grakn.simulation.common.driver;

import org.slf4j.Logger;


public abstract class TransactionalDbOperationFactory<DB_OPERATION extends TransactionalDbOperation> extends DbOperationFactory<DB_OPERATION> {

    public TransactionalDbOperationFactory(Logger logger) {
        super(logger);
    }

    public DB_OPERATION dbOperation() { // Needed for now but will disappear once agents give the operations to the actions rather than the ActionFactory
        return null;
    }

    @Override
    public abstract DB_OPERATION newDbOperation(String tracker, boolean trace);
}
