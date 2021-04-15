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

package grakn.benchmark.common.params;

import grakn.benchmark.common.seed.GeoData;
import grakn.benchmark.common.seed.WordData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Context {

    public static final int AGE_OF_ADULTHOOD = 21;

    private final GeoData geoData;
    private final WordData wordData;
    private final Config config;
    private final boolean isTracing;
    private final boolean isTest;
    private final AtomicInteger iteration;

    private Context(GeoData geoData, WordData wordData, Config config, boolean isTracing, boolean isTest) {
        this.geoData = geoData;
        this.wordData = wordData;
        this.config = config;
        this.isTracing = isTracing;
        this.isTest = isTest;
        this.iteration = new AtomicInteger(0);
    }

    public static Context create(Config config, boolean isTracing, boolean isTest) throws IOException {
        GeoData geoData = GeoData.initialise();
        WordData wordData = WordData.initialise();
        return new Context(geoData, wordData, config, isTracing, isTest);
    }

    public List<Config.Agent> agentConfigs() {
        return config.agents();
    }

    public long seed() {
        return config.randomSeed();
    }

    public void incrementIteration() {
        iteration.incrementAndGet();
    }

    public int scaleFactor() {
        return config.scaleFactor();
    }

    public int iterationNumber() {
        return iteration.get();
    }

    public int iterationMax() {
        return config.iterations();
    }

    public LocalDateTime today() {
        return LocalDateTime.of(LocalDate.ofYearDay(iteration.get(), 1), LocalTime.of(0, 0, 0));
    }

    public GeoData geoData() {
        return geoData;
    }

    public WordData wordData() {
        return wordData;
    }

    public boolean isTracing() {
        return isTracing && config.traceSampling().samplingFunction().apply(iterationNumber());
    }

    public boolean isTest() {
        return isTest;
    }
}
