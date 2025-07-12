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
package com.vaticle.typedb.iam.simulation.common

import com.vaticle.typedb.common.yaml.YAML

class ModelParams private constructor(val populationGrowth: Int, val ageOfFriendship: Int, val ageOfAdulthood: Int, val yearsBeforeParenthood: Int) {
    companion object {
        private const val POPULATION_GROWTH = "populationGrowth"
        private const val AGE_OF_ADULTHOOD = "ageOfAdulthood"
        private const val AGE_OF_FRIENDSHIP = "ageOfFriendship"
        private const val YEARS_BEFORE_PARENTHOOD = "yearsBeforeParenthood"

        fun of(yaml: YAML.Map) = yaml["model"].asMap().let {
            ModelParams(
                populationGrowth = it[POPULATION_GROWTH].asInt().value(),
                ageOfAdulthood = it[AGE_OF_ADULTHOOD].asInt().value(),
                ageOfFriendship = it[AGE_OF_FRIENDSHIP].asInt().value(),
                yearsBeforeParenthood = it[YEARS_BEFORE_PARENTHOOD].asInt().value(),
            )
        }
    }
}
