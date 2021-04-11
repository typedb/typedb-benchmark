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

package grakn.benchmark.simulation.common;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class GeoData {

    private static final File CURRENCIES_FILE = Paths.get("simulation/data/currencies.csv").toFile();
    private static final File CONTINENTS_FILE = Paths.get("simulation/data/continents.csv").toFile();
    private static final File COUNTRIES_FILE = Paths.get("simulation/data/countries.csv").toFile();
    private static final File COUNTY_LANGUAGES_FILE = Paths.get("simulation/data/country_languages.csv").toFile();
    private static final File CITIES_FILE = Paths.get("simulation/data/cities.csv").toFile();
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.withEscape('\\').withIgnoreSurroundingSpaces().withNullString("");

    private final Global global;

    public GeoData(Global global) {
        this.global = global;
    }

    public static GeoData initialise() throws IOException {
        Global global = new Global();
        Map<String, Currency> currencies = new HashMap<>();
        Map<String, Continent> continents = new HashMap<>();
        Map<String, Country> countries = new HashMap<>();
        initialiseCurrencies(global, currencies); // TODO: we won't need this if we use java.util.Currency
        initialiseContinents(global, continents);
        initialiseCountries(continents, countries, currencies);
        initialiseCities(countries);

        return new GeoData(global);
    }

    private static void initialiseCurrencies(Global global, Map<String, Currency> currencies) throws IOException {
        parse(CURRENCIES_FILE).forEach(record -> {
            String name = record.get(0);
            String symbol = record.get(1);
            Currency currency = new Currency(name, symbol);
            global.addCurrency(currency);
            currencies.put(name, currency);
        });
    }

    private static void initialiseContinents(Global global, Map<String, Continent> continents) throws IOException {
        parse(CONTINENTS_FILE).forEach(record -> {
            String name = record.get(0);
            Continent continent = new Continent(name);
            global.addContinent(continent);
            continents.put(name, continent);
        });
    }

    private static void initialiseCountries(Map<String, Continent> continents, Map<String, Country> countries,
                                            Map<String, Currency> currencies) throws IOException {
        parse(COUNTRIES_FILE).forEach(record -> {
            String name = record.get(0);
            Continent continent = continents.get(record.get(1));
            Currency currency = currencies.get(record.get(2));
            Country country = new Country(name, continent, currency);
            continent.addCountry(country);
            countries.put(name, country);
        });
        parse(COUNTY_LANGUAGES_FILE).forEach(record -> {
            Country country = countries.get(record.get(0));
            String language = record.get(1);
            country.addLanguage(language);
        });
    }

    private static void initialiseCities(Map<String, Country> countries) throws IOException {
        parse(CITIES_FILE).forEach(record -> {
            String name = record.get(0);
            Country country = countries.get(record.get(1));
            City city = new City(name, country);
            country.addCity(city);
        });
    }

    private static CSVParser parse(File csvFile) throws IOException {
        return CSVParser.parse(csvFile, StandardCharsets.UTF_8, CSV_FORMAT);
    }

    public Global global() {
        return global;
    }

    public List<Currency> currencies() {
        return global.currencies();
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

        Global() {
            continents = new ArrayList<>();
            currencies = new ArrayList<>();
        }

        void addContinent(Continent continent) {
            continents.add(continent);
        }

        void addCurrency(Currency currency) {
            currencies.add(currency);
        }

        public List<Currency> currencies() {
            return currencies;
        }

        public List<Continent> continents() {
            return continents;
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
    }

    public static class Continent implements Region {

        private final String name;
        private final List<Country> countries;

        Continent(String name) {
            this.name = name;
            this.countries = new ArrayList<>();
        }

        void addCountry(Country country) {
            countries.add(country);
        }

        public List<Country> countries() {
            return countries;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String tracker() {
            return Tracker.of(this);
        }

        @Override
        public String group() {
            return this.name();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Country implements Region {
        private final String name;
        private final Continent continent;
        private final Currency currency;
        private final List<City> cities;
        private final List<String> languages;

        Country(String name, Continent continent, Currency currency) {
            this.name = name;
            this.continent = continent;
            this.currency = currency;
            this.languages = new ArrayList<>();
            this.cities = new ArrayList<>();
        }

        void addLanguage(String language) {
            languages.add(language);
        }

        void addCity(City city) {
            cities.add(city);
        }

        public Continent continent() {
            return continent;
        }

        public Currency currency() {
            return currency;
        }

        public List<String> languages() {
            return languages;
        }

        public List<City> cities() {
            return cities;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String tracker() {
            return Tracker.of(this.continent(), this);
        }

        @Override
        public String group() {
            return continent.name();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class City implements Region {

        private final String name;
        private final Country country;

        City(String name, Country country) {
            this.name = name;
            this.country = country;
        }

        public Country country() {
            return country;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String tracker() {
            return Tracker.of(country().continent(), country(), this);
        }

        @Override
        public String group() {
            return this.country.continent.name();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Tracker {
        public static String of(Object... items) {
            return Stream.of(items).map(Object::toString).collect(Collectors.joining(":"));
        }
    }

    public static class Currency {
        // TODO: should we should replace this class with java.util.Currency ?

        private final String symbol;
        private final String name;

        public Currency(String name, String symbol) {
            this.name = name;
            this.symbol = symbol;
        }

        public String name() {
            return name;
        }

        public String symbol() {
            return symbol;
        }
    }
}
