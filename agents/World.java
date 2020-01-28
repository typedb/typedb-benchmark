package grakn.simulation.agents;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class World {

    public static final int AGE_OF_ADULTHOOD = 2;

    private List<Continent> continents = new ArrayList<>();

    private Map<String, Continent> continentMap = new HashMap<>();
    private Map<String, Country> countryMap = new HashMap<>();
    private Map<String, City> cityMap = new HashMap<>();

    private List<FemaleForename> femaleForenames = new ArrayList<>();
    private List<MaleForename> maleForenames = new ArrayList<>();
    private List<Surname> surnames = new ArrayList<>();

    public World(Path continentsPath, Path countriesPath, Path citiesPath, Path femaleForenamesPath, Path maleForenamesPath, Path surnamesPath) throws IOException {
        iterateCSV(continentsPath, Continent::new);
        iterateCSV(countriesPath, Country::new);
        iterateCSV(citiesPath, City::new);
        iterateCSV(femaleForenamesPath, FemaleForename::new);
        iterateCSV(maleForenamesPath, MaleForename::new);
        iterateCSV(surnamesPath, Surname::new);
    }

    private static void iterateCSV(Path path, Consumer<CSVRecord> action) throws IOException {
        CSVParser.parse(path, StandardCharsets.UTF_8, CSVFormat.DEFAULT).forEach(action);
    }

    public Stream<Continent> getContinents() {
        return continents.stream();
    }

    public Stream<Country> getCountries() {
        return continents.stream().flatMap(Continent::getCountries);
    }

    public Stream<City> getCities() {
        return continents.stream().flatMap(Continent::getCountries).flatMap(Country::getCities);
    }

    public List<String> getFemaleForenames() {
        return Collections.unmodifiableList(femaleForenames.stream().map(World.FemaleForename::getValue).collect(toList()));
    }

    public List<String> getMaleForenames() {
        return Collections.unmodifiableList(maleForenames.stream().map(World.MaleForename::getValue).collect(toList()));
    }

    public List<String> getSurnames() {
        return Collections.unmodifiableList(surnames.stream().map(World.Surname::getValue).collect(toList()));
    }

    public class Continent {
        private String continentName;
        private List<Country> countries = new ArrayList<>();

        private Continent(CSVRecord record) {
            continentName = record.get(0);
            continents.add(this);
            continentMap.put(continentName, this);
        }

        public String getName() {
            return continentName;
        }

        public Stream<Country> getCountries() {
            return countries.stream();
        }
    }

    public class Country {
        private String countryName;
        private Continent continent;
        private List<City> cities = new ArrayList<>();

        public Country(CSVRecord record) {
            countryName = record.get(0);
            continent = continentMap.get(record.get(1));
            continent.countries.add(this);
            countryMap.put(countryName, this);
        }

        public String getName() {
            return countryName;
        }

        public Continent getContinent() {
            return continent;
        }

        public Stream<City> getCities() {
            return cities.stream();
        }
    }

    public class FemaleForename {
        private String value;

        public FemaleForename(CSVRecord record) {
            value = record.get(0);
            femaleForenames.add(this);
        }

        public String getValue() {
            return value;
        }
    }

    public class MaleForename {
        private String value;

        public MaleForename(CSVRecord record) {
            value = record.get(0);
            maleForenames.add(this);
        }

        public String getValue() {
            return value;
        }
    }

    public class Surname {
        private String value;

        public Surname(CSVRecord record) {
            value = record.get(0);
            surnames.add(this);
        }

        public String getValue() {
            return value;
        }
    }

    public class City {
        private String cityName;
        private Country country;

        public City(CSVRecord record) {
            cityName = record.get(0);
            country = countryMap.get(record.get(1));
            country.cities.add(this);
            cityMap.put(cityName, this);
        }

        public String getName() {
            return cityName;
        }

        public Country getCountry() {
            return country;
        }
    }
}
