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
package com.vaticle.typedb.benchmark.common.seed

import com.vaticle.typedb.benchmark.common.concept.City
import com.vaticle.typedb.benchmark.common.concept.Continent
import com.vaticle.typedb.benchmark.common.concept.Country
import com.vaticle.typedb.benchmark.common.concept.Currency
import com.vaticle.typedb.benchmark.common.concept.Global
import com.vaticle.typedb.benchmark.common.concept.University
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

class SeedData(val global: Global) {
    val continents get() = global.continents

    val countries get(): List<Country> {
        return continents.flatMap { it.countries }
    }

    val cities get(): List<City> {
        return countries.flatMap { it.cities }
    }

    val universities get(): List<University> {
        return countries.flatMap { it.universities }
    }

    class Words {
        val adjectives = mutableListOf<String>()
        val nouns = mutableListOf<String>()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SeedData::class.java)
        private val ADJECTIVES_FILE = Paths.get("data/adjectives.csv").toFile()
        private val CITIES_FILE = Paths.get("data/cities.csv").toFile()
        private val CONTINENTS_FILE = Paths.get("data/continents.csv").toFile()
        private val COUNTRIES_FILE = Paths.get("data/countries.csv").toFile()
        private val CURRENCIES_FILE = Paths.get("data/currencies.csv").toFile()
        private val FIRST_NAMES_FEMALE_FILE = Paths.get("data/first-names-female.csv").toFile()
        private val FIRST_NAMES_MALE_FILE = Paths.get("data/first-names-male.csv").toFile()
        private val LAST_NAMES_FILE = Paths.get("data/last-names.csv").toFile()
        private val NOUNS_FILE = Paths.get("data/nouns.csv").toFile()
        private val UNIVERSITIES_FILE = Paths.get("data/universities.csv").toFile()
        private val CSV_FORMAT = CSVFormat.DEFAULT.withEscape('\\').withIgnoreSurroundingSpaces().withNullString("")

        fun initialise(): SeedData {
            val global = Global()
            val continents = mutableMapOf<String, Continent>()
            val countries = mutableMapOf<String, Country>()
            initialiseContinents(global, continents)
            initialiseCountries(continents, countries)
            initialiseCurrencies(countries)
            initialiseCities(countries)
            initialiseUniversities(countries)
            initialiseLastNames(continents)
            initialiseFemaleFirstNames(continents)
            initialiseMaleFirstNames(continents)
            val words = Words()
            initialiseAdjectives(words)
            initialiseNouns(words)
            prune(global)
            return SeedData(global)
        }

        private fun initialiseContinents(global: Global, continents: MutableMap<String, Continent>) {
            parse(CONTINENTS_FILE).forEach { record: CSVRecord ->
                val code = record[0]
                val name = record[1]
                val continent = Continent(code, name)
                global.continents += continent
                continents[code] = continent
            }
        }

        private fun initialiseCountries(continents: Map<String, Continent>, countries: MutableMap<String, Country>) {
            parse(COUNTRIES_FILE).forEach { record: CSVRecord ->
                val code = record[0]
                val name = record[1]
                val continent = requireNotNull(continents[record[2]])
                val country = Country(code, name, continent)
                continent.countries += country
                countries[code] = country
            }
        }

        private fun initialiseCurrencies(countries: Map<String, Country>) {
            val currencies: MutableMap<String, Currency> = HashMap()
            parse(CURRENCIES_FILE).forEach { record: CSVRecord ->
                val code = record[0]
                val name = record[1]
                val country = requireNotNull(countries[record[2]])
                val currency = currencies.computeIfAbsent(code) { Currency(it, name) }
                country.currencies += currency
            }
        }

        private fun initialiseCities(countries: Map<String, Country>) {
            parse(CITIES_FILE).forEach { record: CSVRecord ->
                val code = record[0]
                val name = record[1]
                val country = requireNotNull(countries[record[2]])
                val city = City(code, name, country)
                country.cities += city
            }
        }

        private fun initialiseUniversities(countries: Map<String, Country>) {
            parse(UNIVERSITIES_FILE).forEach { record: CSVRecord ->
                val name = record[0]
                val country = requireNotNull(countries[record[1]])
                val university = University(name, country)
                country.universities += university
            }
        }

        private fun initialiseLastNames(continents: Map<String, Continent>) {
            parse(LAST_NAMES_FILE).forEach { record: CSVRecord ->
                val name = record[0]
                val continent = requireNotNull(continents[record[1]])
                continent.commonLastNames += name
            }
        }

        private fun initialiseFemaleFirstNames(continents: Map<String, Continent>) {
            parse(FIRST_NAMES_FEMALE_FILE).forEach { record: CSVRecord ->
                val name = record[0]
                val continent = requireNotNull(continents[record[1]])
                continent.commonFemaleFirstNames += name
            }
        }

        private fun initialiseMaleFirstNames(continents: Map<String, Continent>) {
            parse(FIRST_NAMES_MALE_FILE).forEach { record: CSVRecord ->
                val name = record[0]
                val continent = requireNotNull(continents[record[1]])
                continent.commonMaleFirstNames += name
            }
        }

        private fun initialiseAdjectives(words: Words) {
            words.adjectives += parse(ADJECTIVES_FILE).map { record: CSVRecord -> record[0] }
        }

        private fun initialiseNouns(words: Words) {
            words.nouns += parse(NOUNS_FILE).map { record: CSVRecord -> record[0] }
        }

        private fun prune(global: Global) {
            val continents = global.continents.listIterator()
            while (continents.hasNext()) {
                val continent = continents.next()
                if (continent.countries.isEmpty()) {
                    continents.remove()
                    LOG.warn("The continent '{}' is excluded as it has no countries in the seed dataset", continent)
                } else if (continent.commonLastNames.isEmpty() ||
                    continent.commonFemaleFirstNames.isEmpty() ||
                    continent.commonMaleFirstNames.isEmpty()
                ) {
                    continents.remove()
                    LOG.warn(
                        "The continent '{}' is excluded as it has no first/last names in the seed dataset",
                        continent
                    )
                } else prune(continent)
            }
        }

        private fun prune(continent: Continent) {
            val countries = continent.countries.listIterator()
            while (countries.hasNext()) {
                val country = countries.next()
                if (country.cities.isEmpty()) {
                    countries.remove()
                    LOG.warn("The country {} is excluded as has no cities in the seed dataset", country)
                }
            }
        }

        private fun parse(csvFile: File): CSVParser {
            return CSVParser.parse(csvFile, StandardCharsets.UTF_8, CSV_FORMAT)
        }

        fun buildTracker(vararg items: Any?): String {
            return items.joinToString(":")
        }
    }
}
