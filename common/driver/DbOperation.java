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

package grakn.benchmark.common.driver;

import grakn.benchmark.common.utils.Trace;

import java.util.function.Supplier;

public abstract class DbOperation implements AutoCloseable {

    protected final String tracker;
    protected final long iteration;
    private final boolean trace;

    public DbOperation(String tracker, long iteration, boolean trace) {
        this.iteration = iteration;
        this.tracker = tracker;
        this.trace = trace;
    }

    public abstract void close();

    public abstract void save();

    protected <T> T trace(Supplier<T> method, String traceName) {
        return Trace.trace(method, traceName, trace);
    }
}
