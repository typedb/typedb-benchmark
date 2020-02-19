package grakn.simulation.agents;

import grakn.simulation.agents.base.AgentItem;
import grakn.simulation.common.LogWrapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Stream;

public class World {

    static final int AGE_OF_ADULTHOOD = 2;
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

    public World(Path continentsPath, Path countriesPath, Path citiesPath, Path femaleForenamesPath, Path maleForenamesPath, Path surnamesPath, Path adjectivesPath, Path nounsPath) throws IOException {
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

    public List<String> getAdjectives() {
        return adjectives;
    }

    public List<String> getNouns() {
        return nouns;
    }

    public class Continent implements AgentItem {
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

        @Override
        public String getTracker() {
            return getName();
        }

        @Override
        public String getSessionKey() {
            return continentName;
        }
    }

    public class Country implements AgentItem {
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

        @Override
        public String getTracker() {
            return getContinent().getTracker() + ":" + getName();
        }

        @Override
        public String getSessionKey() {
            return continent.getSessionKey();
        }
    }

    public class City implements AgentItem {
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

        @Override
        public String getTracker() {
            return getCountry().getTracker() + ":" + getName();
        }

        @Override
        public String getSessionKey() {
            return country.getSessionKey();
        }
    }

    public static WorldLogWrapper log(Class<?> clazz) {
        return new WorldLogWrapper(LoggerFactory.getLogger(clazz));
    }

    public static class WorldLogWrapper extends LogWrapper<AgentItem> {

        private WorldLogWrapper(Logger logger) {
            super(logger, AgentItem::getTracker);
        }
    }
}
