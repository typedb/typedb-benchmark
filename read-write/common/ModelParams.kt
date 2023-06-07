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

import com.vaticle.typedb.benchmark.readwrite.common.Util.int
import com.vaticle.typedb.benchmark.readwrite.common.Util.map
import com.vaticle.typedb.common.yaml.YAML

class ModelParams private constructor(val personPerBatch: Int, val friendshipPerBatch: Int, val nPostCodes: Int) {

    companion object {
        private const val PERSONS_PER_RUN = "personsCreatedPerRun"
        private const val FRIENDSHIPS_PER_RUN = "friendshipsCreatedPerRun"
        private const val POST_CODES = "postCodes"

        fun of(yaml: YAML.Map): ModelParams {
            val nPersonPerBatch = int(map(yaml["model"])[PERSONS_PER_RUN])
            val friendshipPerBatch = int(map(yaml["model"])[FRIENDSHIPS_PER_RUN])
            val postCodes = int(map(yaml["model"])[POST_CODES])
            return ModelParams(nPersonPerBatch, friendshipPerBatch, postCodes)
        }
    }
}
