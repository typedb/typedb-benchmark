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

import com.vaticle.typedb.benchmark.common.seed.SeedData
import java.util.Objects

class Country(override val code: String, override val name: String, val continent: Continent) : Region {
    val currencies = mutableListOf<Currency>()
    val cities = mutableListOf<City>()
    val universities = mutableListOf<University>()
    private val hash: Int = Objects.hash(this.code, this.name, this.continent)

    override val tracker get(): String {
        return SeedData.buildTracker(continent.tracker, name)
    }

    override val group get(): String {
        return continent.name
    }

    override fun toString(): String {
        return "$name ($code)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Country
        return this.code == that.code && name == that.name && continent == that.continent
    }

    override fun hashCode(): Int {
        return hash
    }
}
