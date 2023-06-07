/*
 * Copyright (C) 2022 Vaticle
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
package com.vaticle.typedb.benchmark.readwrite.common

import com.vaticle.typedb.benchmark.framework.common.Partition
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

class Context private constructor(config: Config, isTracing: Boolean, isReporting: Boolean):
    com.vaticle.typedb.benchmark.framework.Context<Void?, ModelParams>(null, config, isTracing, isReporting) {

    val partitions : List<DBPartition> = createPartitionList(config.run.partitions)

    private fun createPartitionList(nPartition: Int): List<DBPartition> {
        val pList : ArrayList<DBPartition> = ArrayList(nPartition);
        for (i: Int in 0..nPartition-1) {
            pList.add(DBPartition(i));
        }
        return pList;
    }

    public class DBPartition(
        val partitionId: Int
    ) : Partition {

        val idCtr : AtomicInteger = AtomicInteger()
        override val code: String
            get() = "code-" + partitionId
        override val group: String
            get() = "group-" + partitionId
        override val name: String
            get() = "name-" + partitionId
        override val tracker: String
            get() = "tracker-" + partitionId

    }
    

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        fun create(config: Config, isTracing: Boolean, isReporting: Boolean): Context {
            return Context(config, isTracing, isReporting)
        }
    }
}
