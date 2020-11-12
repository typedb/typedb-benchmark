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

package grakn.simulation.common;

import grakn.simulation.config.Config;
import grakn.simulation.common.driver.TransactionalDbDriver;
import grakn.simulation.common.driver.TransactionalDbOperation;
import grakn.simulation.common.world.World;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class TransactionalSimulation<DB_DRIVER extends TransactionalDbDriver<?, ?, DB_OPERATION>, DB_OPERATION extends TransactionalDbOperation> extends Simulation<DB_DRIVER, DB_OPERATION> {

    public TransactionalSimulation(DB_DRIVER driver, Map<String, Path> initialisationDataPaths, int randomSeed, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        super(driver, initialisationDataPaths, randomSeed, world, agentConfigs, iterationSamplingFunction, test);
    }

    @Override
    protected void closeIteration() {
        driver.closeSessions();
    }

    @Override
    public void close() {
        driver.close();
    }
}
