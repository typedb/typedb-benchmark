/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.common.seed;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class SeedData {

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

    public List<Continent> continents() {
        return global.continents();
    }

    public List<Country> countries() {
        return continents().stream().flatMap(continent -> continent.countries().stream()).collect(toList());
    }

    public List<City> cities() {
        return countries().stream().flatMap(country -> country.cities().stream()).collect(toList());
    }

    public static class Global implements Region {

        private final List<Continent> continents;
        private final List<Currency> currencies;
        private final int hash;

        Global() {
            continents = new ArrayList<>();
            currencies = new ArrayList<>();
            hash = Objects.hash(continents, currencies);
        }

        void addContinent(Continent continent) {
            continents.add(continent);
        }

        public List<Continent> continents() {
            return continents;
        }

        @Override
        public String code() {
            return "G";
        }

        @Override
        public String name() {
            return "global";
        }

        @Override
        public String tracker() {
            return "global";
        }

        @Override
        public String group() {
            return name();
        }

        @Override
        public String toString() {
            return name();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Global that = (Global) o;
            return continents.equals(that.continents) && currencies.equals(that.currencies);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public static class Continent implements Region {

        private final String code;
        private final String name;
        private final List<Country> countries;
        private final int hash;
        private final ArrayList<String> commonLastNames;
        private final ArrayList<String> commonFemaleFirstNames;
        private final ArrayList<String> commonMaleFirstNames;

        Continent(String code, String name) {
            this.code = code;
            this.name = name;
            this.countries = new ArrayList<>();
            this.commonLastNames = new ArrayList<>();
            this.commonFemaleFirstNames = new ArrayList<>();
            this.commonMaleFirstNames = new ArrayList<>();
            this.hash = this.code.hashCode();
        }

        void addCountry(Country country) {
            countries.add(country);
        }

        void addCommonLastName(String name) {
            commonLastNames.add(name);
        }

        void addCommonFemaleFirstName(String name) {
            commonFemaleFirstNames.add(name);
        }

        void addCommonMaleFirstName(String name) {
            commonMaleFirstNames.add(name);
        }

        public List<Country> countries() {
            return countries;
        }

        public ArrayList<String> commonLastNames() {
            return commonLastNames;
        }

        public ArrayList<String> commonFemaleFirstNames() {
            return commonFemaleFirstNames;
        }

        public ArrayList<String> commonMaleFirstNames() {
            return commonMaleFirstNames;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String tracker() {
            return buildTracker(name);
        }

        @Override
        public String group() {
            return this.name();
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Continent that = (Continent) o;
            return this.code.equals(that.code);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public static class Country implements Region {

        private final String code;
        private final String name;
        private final Continent continent;
        private final List<Currency> currencies;
        private final List<City> cities;
        private final int hash;
        private final List<University> universities;

        Country(String code, String name, Continent continent) {
            this.code = code;
            this.name = name;
            this.continent = continent;
            this.currencies = new ArrayList<>();
            this.cities = new ArrayList<>();
            this.universities = new ArrayList<>();
            this.hash = this.code.hashCode();
        }

        void addCurrency(Currency currency) {
            currencies.add(currency);
        }

        void addCity(City city) {
            cities.add(city);
        }

        void addUniversity(University university) {
            universities.add(university);
        }

        public Continent continent() {
            return continent;
        }

        public List<Currency> currencies() {
            return currencies;
        }

        public List<City> cities() {
            return cities;
        }

        public List<University> universities() {
            return universities;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String tracker() {
            return buildTracker(continent.tracker(), name);
        }

        @Override
        public String group() {
            return continent.name();
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Country that = (Country) o;
            return this.code.equals(that.code);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public static class City implements Region {

        private final String name;
        private final Country country;
        private final int hash;
        private final String code;

        City(String code, String name, Country country) {
            this.code = code;
            this.name = name;
            this.country = country;
            this.hash = this.code.hashCode();
        }

        public Country country() {
            return country;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String tracker() {
            return buildTracker(country.tracker(), name);
        }

        @Override
        public String group() {
            return this.country.continent.name();
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            City that = (City) o;
            return this.code.equals(that.code);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public static class University {

        private final String name;
        private final Country country;

        University(String name, Country country) {
            this.name = name;
            this.country = country;
        }

        public String name() {
            return name;
        }

        public Country country() {
            return country;
        }
    }

    public static class Currency {
        // TODO: should we should replace this class with java.util.Currency ?

        private final String code;
        private final String name;
        private final int hash;

        Currency(String code, String name) {
            this.code = code;
            this.name = name;
            this.hash = this.code.hashCode();
        }

        public String code() {
            return code;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return code + " (" + name + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Currency that = (Currency) o;
            return this.code.equals(that.code);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private static class Words {

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