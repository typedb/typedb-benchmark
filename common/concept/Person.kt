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

import java.time.LocalDateTime
import java.util.Objects

class Person constructor(
    val email: String,
    private val firstName: String? = null,
    private val lastName: String? = null,
    private val address: String? = null,
    private val gender: Gender? = null,
    private val birthDate: LocalDateTime? = null
) {
    private val hash = Objects.hash(email, firstName, lastName, address, gender, birthDate)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Person
        return email == that.email && firstName == that.firstName && lastName == that.lastName && address == that.address && gender == that.gender && birthDate == that.birthDate
    }

    override fun hashCode(): Int {
        return hash
    }
}
