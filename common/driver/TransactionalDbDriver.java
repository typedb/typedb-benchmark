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

public abstract class TransactionalDbDriver<TRANSACTION, SESSION, DB_OPERATION extends TransactionalDbOperation> extends DbDriver<DB_OPERATION> {
    public enum TracingLabel {
        OPEN_CLIENT("openClient"),
        CLOSE_CLIENT("closeClient"),
        OPEN_SESSION("openSession"),
        CLOSE_SESSION("closeSession"),
        OPEN_TRANSACTION("openTx"),
        CLOSE_TRANSACTION("closeTx"),
        COMMIT_TRANSACTION("commitTx"),
        EXECUTE("execute"),
        SORTED_EXECUTE("sortedExecute");

        private String name;

        TracingLabel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public abstract SESSION session(String sessionKey);

    public abstract void closeSessions();

    public abstract class Session {
        public abstract TRANSACTION tx();
    }
}
