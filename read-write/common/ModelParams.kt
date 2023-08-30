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

class ModelParams private constructor(val personCreatePerAction: Int, val friendshipCreatePerAction: Int, val tryPersonDeletePerAction: Int, val nPostCodes: Int) {

    companion object {
        private const val PERSON_CREATE_PER_ACTION = "personsCreatedPerAction"
        private const val FRIENDSHIP_CREATE_PER_ACTION = "friendshipsCreatedPerAction"
        private const val TRY_PERSON_DELETE_PER_ACTION = "tryPersonsDeletedPerAction"
        private const val POST_CODES = "postCodes"

        fun of(yaml: YAML.Map): ModelParams {
            val personCreatePerAction = int(map(yaml["model"])[PERSON_CREATE_PER_ACTION])
            val friendshipCreatePerAction = int(map(yaml["model"])[FRIENDSHIP_CREATE_PER_ACTION])
            val tryPersonDeletePerAction = intGetOrDefault(yaml["model"].asMap(), TRY_PERSON_DELETE_PER_ACTION, 0)
            val postCodes = int(map(yaml["model"])[POST_CODES])
            return ModelParams(personCreatePerAction, friendshipCreatePerAction, tryPersonDeletePerAction, postCodes)
        }

        fun intGetOrDefault(yaml: YAML.Map, key: String, default: Int): Int {
            return if (yaml[key] == null) default else int(yaml[key])
        }
    }
}
