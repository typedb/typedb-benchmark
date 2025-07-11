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

import com.vaticle.typedb.iam.simulation.common.concept.City
import com.vaticle.typedb.iam.simulation.common.concept.Continent
import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.common.concept.Currency
import com.vaticle.typedb.iam.simulation.common.concept.Global
import com.vaticle.typedb.iam.simulation.common.concept.University
import com.vaticle.typedb.simulation.common.Util.parse
import com.vaticle.typedb.simulation.common.Util.readPair
import com.vaticle.typedb.simulation.common.Util.readSingle
import com.vaticle.typedb.simulation.common.Util.readTriple
import mu.KotlinLogging
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
        private val LOGGER = KotlinLogging.logger {}
        private val ADJECTIVES_FILE = Paths.get("simulation/data/adjectives.csv").toFile()
        private val CITIES_FILE = Paths.get("simulation/data/cities.csv").toFile()
        private val CONTINENTS_FILE = Paths.get("simulation/data/continents.csv").toFile()
        private val COUNTRIES_FILE = Paths.get("simulation/data/countries.csv").toFile()
        private val CURRENCIES_FILE = Paths.get("simulation/data/currencies.csv").toFile()
        private val FIRST_NAMES_FEMALE_FILE = Paths.get("simulation/data/first-names-female.csv").toFile()
        private val FIRST_NAMES_MALE_FILE = Paths.get("simulation/data/first-names-male.csv").toFile()
        private val LAST_NAMES_FILE = Paths.get("simulation/data/last-names.csv").toFile()
        private val NOUNS_FILE = Paths.get("simulation/data/nouns.csv").toFile()
        private val UNIVERSITIES_FILE = Paths.get("simulation/data/universities.csv").toFile()

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
            parse(CONTINENTS_FILE).map { it.readPair() }.forEach { (code, name) ->
                val continent = Continent(code, name)
                global.continents += continent
                continents[code] = continent
            }
        }

        private fun initialiseCountries(continents: Map<String, Continent>, countries: MutableMap<String, Country>) {
            parse(COUNTRIES_FILE).map { it.readTriple() }.forEach { (code, name, continentCode) ->
                val continent = requireNotNull(continents[continentCode])
                val country = Country(code, name, continent)
                continent.countries += country
                countries[code] = country
            }
        }

        private fun initialiseCurrencies(countries: Map<String, Country>) {
            val currencies = mutableMapOf<String, Currency>()
            parse(CURRENCIES_FILE).map { it.readTriple() }.forEach { (code, name, countryCode) ->
                val country = requireNotNull(countries[countryCode])
                val currency = currencies.computeIfAbsent(code) { Currency(it, name) }
                country.currencies += currency
            }
        }

        private fun initialiseCities(countries: Map<String, Country>) {
            parse(CITIES_FILE).map { it.readTriple() }.forEach { (code, name, countryCode) ->
                val country = requireNotNull(countries[countryCode])
                val city = City(code, name, country)
                country.cities += city
            }
        }

        private fun initialiseUniversities(countries: Map<String, Country>) {
            parse(UNIVERSITIES_FILE).map { it.readPair() }.forEach { (name, countryCode) ->
                val country = requireNotNull(countries[countryCode])
                val university = University(name, country)
                country.universities += university
            }
        }

        private fun initialiseLastNames(continents: Map<String, Continent>) {
            parse(LAST_NAMES_FILE).map { it.readPair() }.forEach { (name, continentCode) ->
                val continent = requireNotNull(continents[continentCode])
                continent.commonLastNames += name
            }
        }

        private fun initialiseFemaleFirstNames(continents: Map<String, Continent>) {
            parse(FIRST_NAMES_FEMALE_FILE).map { it.readPair() }.forEach { (name, continentCode) ->
                val continent = requireNotNull(continents[continentCode])
                continent.commonFemaleFirstNames += name
            }
        }

        private fun initialiseMaleFirstNames(continents: Map<String, Continent>) {
            parse(FIRST_NAMES_MALE_FILE).map { it.readPair() }.forEach { (name, continentCode) ->
                val continent = requireNotNull(continents[continentCode])
                continent.commonMaleFirstNames += name
            }
        }

        private fun initialiseAdjectives(words: Words) {
            words.adjectives += parse(ADJECTIVES_FILE).map { it.readSingle() }
        }

        private fun initialiseNouns(words: Words) {
            words.nouns += parse(NOUNS_FILE).map { it.readSingle() }
        }

        private fun prune(global: Global) {
            val continents = global.continents.listIterator()
            while (continents.hasNext()) {
                val continent = continents.next()
                if (continent.countries.isEmpty()) {
                    continents.remove()
                    LOGGER.warn("The continent '{}' is excluded as it has no countries in the seed dataset", continent)
                } else if (continent.commonLastNames.isEmpty() ||
                    continent.commonFemaleFirstNames.isEmpty() ||
                    continent.commonMaleFirstNames.isEmpty()
                ) {
                    continents.remove()
                    LOGGER.warn(
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
                    LOGGER.warn("The country {} is excluded as has no cities in the seed dataset", country)
                }
            }
        }
    }
}
