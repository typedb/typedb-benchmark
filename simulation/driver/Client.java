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

package grakn.benchmark.simulation.driver;

import grakn.benchmark.simulation.world.Region;
import org.slf4j.Logger;

public abstract class Client<SESSION extends Session<TX>, TX extends Transaction> {

    public abstract SESSION session(String sessionKey);

    public abstract void closeSessions();

    public abstract Session<TX> session(Region region);

    public abstract void printStatistics(Logger LOG);

    public abstract void close();

    public enum TracingLabel {
        OPEN_CLIENT("open_client"),
        CLOSE_CLIENT("close_client"),
        OPEN_SESSION("open_session"),
        CLOSE_SESSION("close_session"),
        OPEN_TRANSACTION("open_tx"),
        CLOSE_TRANSACTION("close_tx"),
        COMMIT_TRANSACTION("commit_tx"),
        EXECUTE("execute"),
        SORTED_EXECUTE("sorted_execute"),
        EXECUTE_ASYNC("execute_async");

        private String name;

        TracingLabel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
