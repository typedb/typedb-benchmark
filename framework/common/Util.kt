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
package com.vaticle.typedb.benchmark.framework.common

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant

object Util {
    private val CSV_FORMAT = CSVFormat.DEFAULT.withEscape('\\').withIgnoreSurroundingSpaces().withNullString("")

    fun printDuration(start: Instant, end: Instant): String {
        return Duration.between(start, end).toString()
            .substring(2)
            .replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
            .lowercase()
    }

    fun parse(csvFile: File): CSVParser {
        return CSVParser.parse(csvFile, StandardCharsets.UTF_8, CSV_FORMAT)
    }

    fun buildTracker(vararg items: Any?): String {
        return items.joinToString(":")
    }

    fun CSVRecord.readSingle(): String {
        return requireNotNull(this[0]) { "Expected CSV record '$this' to have at least 1 element, but found ${count()}" }
    }

    fun CSVRecord.readPair(): Pair<String, String> {
        return listOf(this[0], this[1]).map {
            requireNotNull(it) { "Expected CSV record '$this' to have at least 2 elements, but found ${count()}" }
        }.let { Pair(it[0], it[1]) }
    }

    fun CSVRecord.readTriple(): Triple<String, String, String> {
        return listOf(this[0], this[1], this[2]).map {
            requireNotNull(it) { "Expected CSV record '$this' to have at least 3 elements, but found ${count()}" }
        }.let { Triple(it[0], it[1], it[2]) }
    }
}
