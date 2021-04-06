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

public abstract class DbDriver<DB_OPERATION extends DbOperation> {
//    TODO should have enums for tracing for the universal names of DB operations that we want to compare across
//     different backends

    public enum TracingLabel {
        START_OP("startOp"),
        CLOSE_OP("closeOp"),
        SAVE_OP("saveOp");

        private String name;

        TracingLabel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public abstract void close();

    public abstract void printStatistics(Logger LOG);

    public abstract DbOperationFactory<DB_OPERATION> getDbOperationFactory(Region region, Logger logger);
}
