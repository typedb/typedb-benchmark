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

import grakn.benchmark.common.seed.SeedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Context {

    public static final int AGE_OF_ADULTHOOD = 21;
    private static final Logger LOG = LoggerFactory.getLogger(Context.class);

    private final boolean isTracing;
    private final boolean isReporting;
    private final Config config;
    private final SeedData seedData;
    private final AtomicInteger iteration;
    private final ExecutorService executor;

    private Context(SeedData seedData, Config config, boolean isTracing, boolean isReporting) {
        this.seedData = seedData;
        this.config = config;
        this.isTracing = isTracing;
        this.isReporting = isReporting;
        this.iteration = new AtomicInteger(1);
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static Context create(Config config, boolean isTracing, boolean isTest) throws IOException {
        SeedData seedData = SeedData.initialise();
        LOG.info("Total number of continents in seed: {}", seedData.continents().size());
        LOG.info("Total number of countries in seed: {}", seedData.countries().size());
        LOG.info("Total number of cities in seed: {}", seedData.cities().size());
        LOG.info("Total number of universities in seed: {}", seedData.universities().size());

        return new Context(seedData, config, isTracing, isTest);
    }

    public ExecutorService executor() {
        return executor;
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

    public String databaseName() {
        return config.databaseName();
    }

    public int iterationNumber() {
        return iteration.get();
    }

    public int iterationMax() {
        return config.iterations();
    }

    public LocalDateTime today() {
        return LocalDateTime.of(LocalDate.ofYearDay(2000 + iteration.get(), 1), LocalTime.of(0, 0, 0));
    }

    public SeedData seedData() {
        return seedData;
    }

    public boolean isTracing() {
        return isTracing && config.traceSampling().samplingFunction().apply(iterationNumber());
    }

    public boolean isReporting() {
        return isReporting;
    }
}
