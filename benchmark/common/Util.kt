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

import com.vaticle.typedb.common.yaml.YAML
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

object Util {
    fun printDuration(start: Instant, end: Instant): String {
        return Duration.between(start, end).toString()
            .substring(2)
            .replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
            .lowercase()
    }

    fun boolean(yaml: YAML?): Boolean = yaml!!.asBoolean().value()
    fun int(yaml: YAML?): Int = yaml!!.asInt().value()
    fun double(yaml: YAML?): Double = yaml!!.asDouble().value()
    fun string(yaml: YAML?): String = yaml!!.asString().value()
    fun map(yaml: YAML?): Map<String, YAML> = yaml!!.asMap().content()
    fun list(yaml: YAML?): List<YAML> = yaml!!.asList().content()

    fun iterationDate(iterationNumber: Int): LocalDateTime {
        return LocalDateTime.of(2000, 1, 1, 0, 0).plusDays(iterationNumber.toLong())
    }
}
