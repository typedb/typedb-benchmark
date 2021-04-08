/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.simulation.common;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

public class SimulationContext {

    @Nullable
    private Function<Integer, Boolean> samplingFunction;

    private final World world;
    private final boolean isTest;
    private int iteration;

    private SimulationContext(World world, boolean isTest) {
        this.world = world;
        this.isTest = isTest;
        this.iteration = 0;
    }

    public static SimulationContext create(World world, boolean isTest) {
        return new SimulationContext(world, isTest);
    }

    public void enableTracing(Function<Integer, Boolean> samplingFunction) {
        this.samplingFunction = samplingFunction;
    }

    public void incrementIteration() {
        iteration++;
    }

    public int iteration() {
        return iteration;
    }

    public LocalDateTime today() {
        return LocalDateTime.of(LocalDate.ofYearDay(iteration, 1), LocalTime.of(0, 0, 0));
    }

    public World world() {
        return world;
    }

    public boolean isTracing() {
        return samplingFunction != null && samplingFunction.apply(iteration());
    }

    public boolean isTest() {
        return isTest;
    }
}
