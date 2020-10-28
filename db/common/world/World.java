package grakn.simulation.db.common.world;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World implements Region {

    public final int AGE_OF_ADULTHOOD = 2;
    private final int scaleFactor;
    private Path logDirPath;

    private List<Continent> continents = new ArrayList<>();

    private Map<String, Continent> continentMap = new HashMap<>();
    private Map<String, Country> countryMap = new HashMap<>();
    private Map<String, City> cityMap = new HashMap<>();

    private final List<String> femaleForenames;
    private final List<String> maleForenames;
    private final List<String> surnames;
    private final List<String> adjectives;
    private final List<String> nouns;

    public World(int scaleFactor, Path continentsPath, Path countriesPath, Path citiesPath, Path femaleForenamesPath, Path maleForenamesPath, Path surnamesPath, Path adjectivesPath, Path nounsPath) throws IOException {

        this.scaleFactor = scaleFactor;

        try {
            this.logDirPath = Paths.get(System.getenv("LOG_DIR_PATH"));
        } catch (NullPointerException n){
            this.logDirPath = null;
        }

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

        List<String> adjectives = new ArrayList<>();
        iterateCSV(adjectivesPath, r -> adjectives.add(r.get(0)));
        this.adjectives = Collections.unmodifiableList(adjectives);

        List<String> nouns = new ArrayList<>();
        iterateCSV(nounsPath, r -> nouns.add(r.get(0)));
        this.nouns = Collections.unmodifiableList(nouns);
    }

    private static void iterateCSV(Path path, Consumer<CSVRecord> action) throws IOException {
        CSVParser.parse(path, StandardCharsets.UTF_8, CSVFormat.DEFAULT).forEach(action);
    }

    public static World initialise(int scaleFactor, Map<String, Path> files) {
        World world;
        try {
            // TODO: create WorldBuilder to reduce the number of constructor parameters
            world = new World(
                    scaleFactor,
                    files.get("continents.csv"),
                    files.get("countries.csv"),
                    files.get("cities.csv"),
                    files.get("female_forenames.csv"),
                    files.get("male_forenames.csv"),
                    files.get("surnames.csv"),
                    files.get("adjectives.csv"),
                    files.get("nouns.csv")
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
        return world;
    }

    public Stream<Continent> getContinents() {
        return continents.stream();
    }

    public Stream<Country> getCountries() {
        return continents.stream().flatMap(Continent::countries);
    }

    public Stream<City> getCities() {
        return continents.stream().flatMap(Continent::countries).flatMap(Country::cities);
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

    public List<String> getAdjectives() {
        return adjectives;
    }

    public List<String> getNouns() {
        return nouns;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public String name() {
        return "world";
    }

    @Override
    public String tracker() {
        return "world";
    }

    @Override
    public String topLevelName() {
        return name();
    }

    public class Continent implements Region {
        private String continentName;
        private List<Country> countries = new ArrayList<>();

        private Continent(CSVRecord record) {
            continentName = record.get(0);
            continents.add(this);
            continentMap.put(continentName, this);
        }

        @Override
        public String toString() {
            return continentName;
        }

        @Override
        public String name() {
            return continentName;
        }

        public Stream<Country> countries() {
            return countries.stream();
        }

        @Override
        public String tracker() {
            return Tracker.of(this);
        }

        @Override
        public String topLevelName() {
            return this.name();
        }
    }

    public class Country implements Region {
        private String countryName;
        private Continent continent;
        private List<City> cities = new ArrayList<>();

        public Country(CSVRecord record) {
            countryName = record.get(0);
            continent = continentMap.get(record.get(1));
            continent.countries.add(this);
            countryMap.put(countryName, this);
        }

        @Override
        public String toString() {
            return countryName;
        }

        @Override
        public String name() {
            return countryName;
        }

        public Continent continent() {
            return continent;
        }

        public String topLevelName() {
            return continent.name();
        }

        public Stream<City> cities() {
            return cities.stream();
        }

        @Override
        public String tracker() {
            return Tracker.of(this.continent(), this);
        }
    }

    public class City implements Region {
        private String cityName;
        private Country country;

        public City(CSVRecord record) {
            cityName = record.get(0);
            country = countryMap.get(record.get(1));
            country.cities.add(this);
            cityMap.put(cityName, this);
        }

        @Override
        public String toString() {
            return cityName;
        }

        @Override
        public String name() {
            return cityName;
        }

        public Country country() {
            return country;
        }

        @Override
        public String tracker() {
            return Tracker.of(country().continent(), country(), this);
        }

        @Override
        public String topLevelName() {
            return this.country.continent.name();
        }
    }

    public static class Tracker {
        public static String of(Object... items) {
            return Stream.of(items).map(Object::toString).collect(Collectors.joining(":"));
        }
    }
}
