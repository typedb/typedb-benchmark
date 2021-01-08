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

package grakn.benchmark.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class ComparisonTestRunner extends BlockJUnit4ClassRunner {
    private final int iteration;

    public ComparisonTestRunner(Class<?> aClass, int iteration) throws InitializationError {
        super(aClass);
        this.iteration = iteration;
    }

    @Override
    protected String testName(FrameworkMethod method) {
        return method.getName() + "-it" + iteration;
    }

    @Override
    protected String getName() {
        return super.getName() + "-iteration" + iteration;
    }
}
