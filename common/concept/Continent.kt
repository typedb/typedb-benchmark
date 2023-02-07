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

import com.vaticle.typedb.benchmark.common.concept.Gender.FEMALE
import com.vaticle.typedb.benchmark.common.concept.Gender.MALE
import com.vaticle.typedb.benchmark.common.seed.SeedData

class Continent(override val code: String, override val name: String) : Region {
    val countries = mutableListOf<Country>()
    val commonLastNames = mutableListOf<String>()
    val commonFemaleFirstNames = mutableListOf<String>()
    val commonMaleFirstNames = mutableListOf<String>()
    private val hash: Int = this.code.hashCode()

    fun commonFirstNames(gender: Gender) = when (gender) {
        MALE -> commonMaleFirstNames
        FEMALE -> commonFemaleFirstNames
    }

    override val tracker get(): String {
        return SeedData.buildTracker(name)
    }

    override val group get(): String {
        return name
    }

    override fun toString(): String {
        return "$name($code)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Continent
        return this.code == that.code
    }

    override fun hashCode(): Int {
        return hash
    }
}
