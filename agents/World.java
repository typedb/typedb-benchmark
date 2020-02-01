package grakn.simulation.agents;

import graql.lang.query.GraqlQuery;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class World {

    public static final int AGE_OF_ADULTHOOD = 2;
    Path logDirPath;

    private List<Continent> continents = new ArrayList<>();

    private Map<String, Continent> continentMap = new HashMap<>();
    private Map<String, Country> countryMap = new HashMap<>();
    private Map<String, City> cityMap = new HashMap<>();

    private final List<String> femaleForenames;
    private final List<String> maleForenames;
    private final List<String> surnames;

    public World(Path continentsPath, Path countriesPath, Path citiesPath, Path femaleForenamesPath, Path maleForenamesPath, Path surnamesPath, Path logDirPath) throws IOException {
        this.logDirPath = logDirPath;
        iterateCSV(continentsPath, Continent::new);
        iterateCSV(countriesPath, Country::new);
        iterateCSV(citiesPath, City::new);

        List<String> femaleForenames = new ArrayList<>();
        iterateCSV(femaleForenamesPath, r -> femaleForenames.add(r.get(0)));
        this.femaleForenames = Collections.unmodifiableList(femaleForenames);

        List<String> maleForenames = new ArrayList<>();
        iterateCSV(maleForenamesPath, r -> maleForenames.add(r.get(0)));
        this.maleForenames = Collections.unmodifiableList(maleForenames);

        List<String> surnames = new ArrayList<>();
        iterateCSV(surnamesPath, r -> surnames.add(r.get(0)));
        this.surnames = Collections.unmodifiableList(surnames);
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
        return femaleForenames;
    }

    public List<String> getMaleForenames() {
        return maleForenames;
    }

    public List<String> getSurnames() {
        return surnames;
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

    public class City {
        private BufferedWriter logWriter;
        private String cityName;
        private Country country;

        public City(CSVRecord record) {
            cityName = record.get(0);
            country = countryMap.get(record.get(1));
            country.cities.add(this);
            cityMap.put(cityName, this);
            Path path = logDirPath.resolve(cityName + ".txt");
            try {
                logWriter = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(path.toString()), "utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void logQuery(GraqlQuery query) {
            try {
                logWriter.write(query.toString() + "\n---\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void log(String message) {
            try {
                logWriter.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                logWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return cityName;
        }

        public Country getCountry() {
            return country;
        }
    }
}
