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

class Marriage(val wife: Person, val husband: Person, val licence: String, val date: LocalDateTime) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val marriage = other as Marriage
        return wife == marriage.wife && husband == marriage.husband && licence == marriage.licence && date == marriage.date
    }

    override fun hashCode(): Int {
        return Objects.hash(wife, husband, licence, date)
    }
}
