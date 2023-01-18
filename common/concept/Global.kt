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

class Global : Region {
    val continents = mutableListOf<Continent>()

    override val code = "G"
    override val name = "global"
    override val tracker = name
    override val group = name

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        return if (this === other) true else other != null && javaClass == other.javaClass
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }
}
