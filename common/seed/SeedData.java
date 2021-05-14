/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.benchmark.common.seed;

import com.vaticle.typedb.benchmark.common.concept.City;
import com.vaticle.typedb.benchmark.common.concept.Continent;
import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Currency;
import com.vaticle.typedb.benchmark.common.concept.Global;
import com.vaticle.typedb.benchmark.common.concept.University;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class SeedData {

    private static final Logger LOG = LoggerFactory.getLogger(SeedData.class);
    private static final File ADJECTIVES_FILE = Paths.get("data/adjectives.csv").toFile();
    private static final File CITIES_FILE = Paths.get("data/cities.csv").toFile();
    private static final File CONTINENTS_FILE = Paths.get("data/continents.csv").toFile();
    private static final File COUNTRIES_FILE = Paths.get("data/countries.csv").toFile();
    private static final File CURRENCIES_FILE = Paths.get("data/currencies.csv").toFile();
    private static final File FIRST_NAMES_FEMALE_FILE = Paths.get("data/first-names-female.csv").toFile();
    private static final File FIRST_NAMES_MALE_FILE = Paths.get("data/first-names-male.csv").toFile();
    private static final File LAST_NAMES_FILE = Paths.get("data/last-names.csv").toFile();
    private static final File NOUNS_FILE = Paths.get("data/nouns.csv").toFile();
    private static final File UNIVERSITIES_FILE = Paths.get("data/universities.csv").toFile();
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.withEscape('\\').withIgnoreSurroundingSpaces().withNullString("");

    private final Global global;
    private final Words words;

    public SeedData(Global global, Words words) {
        this.global = global;
        this.words = words;
    }

    public static SeedData initialise() throws IOException {
        Global global = new Global();
        Map<String, Continent> continents = new HashMap<>();
        Map<String, Country> countries = new HashMap<>();
        initialiseContinents(global, continents);
        initialiseCountries(continents, countries);
        initialiseCurrencies(countries);
        initialiseCities(countries);
        initialiseUniversities(countries);
        initialiseLastNames(continents);
        initialiseFemaleFirstNames(continents);
        initialiseMaleFirstNames(continents);

        Words words = new Words();
        initialiseAdjectives(words);
        initialiseNouns(words);

        prune(global);

        return new SeedData(global, words);
    }

    private static void initialiseContinents(Global global, Map<String, Continent> continents) throws IOException {
        parse(CONTINENTS_FILE).forEach(record -> {
            String code = record.get(0);
            String name = record.get(1);
            Continent continent = new Continent(code, name);
            global.addContinent(continent);
            continents.put(code, continent);
        });
    }

    private static void initialiseCountries(Map<String, Continent> continents, Map<String, Country> countries) throws IOException {
        parse(COUNTRIES_FILE).forEach(record -> {
            String code = record.get(0);
            String name = record.get(1);
            Continent continent = continents.get(record.get(2));
            Country country = new Country(code, name, continent);
            continent.addCountry(country);
            countries.put(code, country);
        });
    }

    private static void initialiseCurrencies(Map<String, Country> countries) throws IOException {
        Map<String, Currency> currencies = new HashMap<>();
        parse(CURRENCIES_FILE).forEach(record -> {
            String code = record.get(0);
            String name = record.get(1);
            Country country = countries.get(record.get(2));
            Currency currency = currencies.computeIfAbsent(code, c -> new Currency(c, name));
            country.addCurrency(currency);
        });
    }

    private static void initialiseCities(Map<String, Country> countries) throws IOException {
        parse(CITIES_FILE).forEach(record -> {
            String code = record.get(0);
            String name = record.get(1);
            Country country = countries.get(record.get(2));
            City city = new City(code, name, country);
            country.addCity(city);
        });
    }

    private static void initialiseUniversities(Map<String, Country> countries) throws IOException {
        parse(UNIVERSITIES_FILE).forEach(record -> {
            String name = record.get(0);
            Country country = countries.get(record.get(1));
            University university = new University(name, country);
            country.addUniversity(university);
        });
    }

    private static void initialiseLastNames(Map<String, Continent> continents) throws IOException {
        parse(LAST_NAMES_FILE).forEach(record -> {
            String name = record.get(0);
            Continent continent = continents.get(record.get(1));
            continent.addCommonLastName(name);
        });
    }

    private static void initialiseFemaleFirstNames(Map<String, Continent> continents) throws IOException {
        parse(FIRST_NAMES_FEMALE_FILE).forEach(record -> {
            String name = record.get(0);
            Continent continent = continents.get(record.get(1));
            continent.addCommonFemaleFirstName(name);
        });
    }

    private static void initialiseMaleFirstNames(Map<String, Continent> continents) throws IOException {
        parse(FIRST_NAMES_MALE_FILE).forEach(record -> {
            String name = record.get(0);
            Continent continent = continents.get(record.get(1));
            continent.addCommonMaleFirstName(name);
        });
    }

    private static void initialiseAdjectives(Words words) throws IOException {
        parse(ADJECTIVES_FILE).forEach(record -> {
            String adjective = record.get(0);
            words.addAjective(adjective);
        });
    }

    private static void initialiseNouns(Words words) throws IOException {
        parse(NOUNS_FILE).forEach(record -> {
            String adjective = record.get(0);
            words.addNoun(adjective);
        });
    }

    private static void prune(Global global) {
        ListIterator<Continent> continents = global.continents().listIterator();
        while (continents.hasNext()) {
            Continent continent = continents.next();
            if (continent.countries().isEmpty()) {
                continents.remove();
                LOG.warn("The continent '{}' is excluded as it has no countries in the seed dataset", continent);
            } else if (continent.commonLastNames().isEmpty() ||
                    continent.commonFemaleFirstNames().isEmpty() ||
                    continent.commonMaleFirstNames().isEmpty()) {
                continents.remove();
                LOG.warn("The continent '{}' is excluded as it has no first/last names in the seed dataset", continent);
            } else prune(continent);
        }
    }

    private static void prune(Continent continent) {
        ListIterator<Country> countries = continent.countries().listIterator();
        while (countries.hasNext()) {
            Country country = countries.next();
            if (country.cities().isEmpty()) {
                countries.remove();
                LOG.warn("The country {} is excluded as has no cities in the seed dataset", country);
            }
        }
    }

    private static CSVParser parse(File csvFile) throws IOException {
        return CSVParser.parse(csvFile, StandardCharsets.UTF_8, CSV_FORMAT);
    }

    public static String buildTracker(Object... items) {
        return Stream.of(items).map(Object::toString).collect(Collectors.joining(":"));
    }

    public Words words() {
        return words;
    }

    public Global global() {
        return global;
    }

    public ArrayList<Continent> continents() {
        return global.continents();
    }

    public List<Country> countries() {
        return continents().stream().flatMap(continent -> continent.countries().stream()).collect(toList());
    }

    public List<City> cities() {
        return countries().stream().flatMap(country -> country.cities().stream()).collect(toList());
    }

    public List<University> universities() {
        return countries().stream().flatMap(country -> country.universities().stream()).collect(toList());
    }

    public static class Words {

        private final ArrayList<String> adjectives;
        private final ArrayList<String> nouns;

        private Words() {
            adjectives = new ArrayList<>();
            nouns = new ArrayList<>();
        }

        void addAjective(String adjective) {
            adjectives.add(adjective);
        }

        void addNoun(String noun) {
            nouns.add(noun);
        }

        public ArrayList<String> adjectives() {
            return adjectives;
        }

        public ArrayList<String> nouns() {
            return nouns;
        }
    }
}
