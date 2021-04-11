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
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

public class SimulationContext {

    public static final int AGE_OF_ADULTHOOD = 1;

    @Nullable
    private Function<Integer, Boolean> samplingFunction;

    private final GeoData geoData;
    private final WordData wordData;
    private final int scaleFactor;
    private final boolean isTest;
    private int iteration;

    private SimulationContext(GeoData geoData, WordData wordData, int scaleFactor, boolean isTest) {
        this.geoData = geoData;
        this.wordData = wordData;
        this.scaleFactor = scaleFactor;
        this.isTest = isTest;
        this.iteration = 0;
    }

    public static SimulationContext create(int scaleFactor, boolean isTest) throws IOException {
        GeoData geoData = GeoData.initialise();
        WordData wordData = WordData.initialise();
        return new SimulationContext(geoData, wordData, scaleFactor, isTest);
    }

    public void enableTracing(Function<Integer, Boolean> samplingFunction) {
        this.samplingFunction = samplingFunction;
    }

    public void incrementIteration() {
        iteration++;
    }

    public int scaleFactor() {
        return scaleFactor;
    }

    public int iteration() {
        return iteration;
    }

    public LocalDateTime today() {
        return LocalDateTime.of(LocalDate.ofYearDay(iteration, 1), LocalTime.of(0, 0, 0));
    }

    public GeoData geoData() {
        return geoData;
    }

    public WordData wordData() {
        return wordData;
    }

    public boolean isTracing() {
        return samplingFunction != null && samplingFunction.apply(iteration());
    }

    public boolean isTest() {
        return isTest;
    }
}
