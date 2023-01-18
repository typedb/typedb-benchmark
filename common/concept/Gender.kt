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
package com.vaticle.typedb.benchmark.common.concept

enum class Gender(val value: String) {
    MALE("male"),
    FEMALE("female");

    companion object {
        fun of(isMale: Boolean): Gender {
            return if (isMale) MALE else FEMALE
        }

        fun of(gender: String): Gender {
            return when (gender) {
                MALE.value -> MALE
                FEMALE.value -> FEMALE
                else -> throw IllegalArgumentException("Unrecognised Gender: $gender")
            }
        }
    }
}
