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

package com.vaticle.typedb.benchmark.common

import com.vaticle.typedb.benchmark.common.Util.int
import com.vaticle.typedb.benchmark.common.Util.map
import com.vaticle.typedb.common.yaml.YAML

class ModelParams private constructor(val personPerBatch: Int, val friendshipPerBatch: Int) {

    companion object {
        private const val PERSON_PER_ITER = "personPerIteration"
        private const val FRIENDSHIP_PER_ITER = "friendshipPerIteration"

        fun of(yaml: YAML.Map): ModelParams {
            val nPersonPerBatch = int(map(yaml["model"])[PERSON_PER_ITER])
            val friendshipPerBatch = int(map(yaml["model"])[FRIENDSHIP_PER_ITER])
            return ModelParams(nPersonPerBatch, friendshipPerBatch)
        }
    }
}
