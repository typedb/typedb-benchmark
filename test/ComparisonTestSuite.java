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

package grakn.benchmark.test;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static grakn.benchmark.test.BenchmarksForComparison.graknCore;
import static grakn.benchmark.test.BenchmarksForComparison.neo4j;

public class ComparisonTestSuite extends Suite {
    private static final List<Runner> NO_RUNNERS = Collections.emptyList();
    private final List<Runner> runners;
    private final Class<?> klass;
    private static int iteration = 1;

    public ComparisonTestSuite(Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        this.klass = klass;
        this.runners = Collections.unmodifiableList(createRunnersForIterations());
    }

    private List<Runner> createRunnersForIterations() {
        List<Runner> runners = new ArrayList<>();
        for (int i = 1; i <= BenchmarksForComparison.numIterations; i++) {
            try {
                BlockJUnit4ClassRunner runner = new ComparisonTestRunner(klass, i);
                runners.add(runner);
            } catch (InitializationError initializationError) {
                throw new RuntimeException(initializationError);
            }
        }
        return runners;
    }

    protected void runChild(Runner runner, final RunNotifier notifier) {
        iteration++;
        neo4j.iterate();
        graknCore.iterate();
        super.runChild(runner, notifier);
        if (iteration == BenchmarksForComparison.numIterations + 1) {
            graknCore.close();
            neo4j.close();
        }
    }

    protected List<Runner> getChildren() {
        return this.runners;
    }
}
