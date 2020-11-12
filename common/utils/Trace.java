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

package grakn.simulation.common.utils;

import grabl.tracing.client.GrablTracingThreadStatic;

import java.util.function.Supplier;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public class Trace {

    /**
     * A wrapper to trace a method or not according to a supplied boolean
     */
    public static <T> T trace(Supplier<T> methodToTrace, String traceName, boolean trace) {
        if (trace) {
            try (GrablTracingThreadStatic.ThreadTrace threadTrace = traceOnThread(traceName)) {
                return methodToTrace.get();
            }
        } else {
            return methodToTrace.get();
        }
    }
}
