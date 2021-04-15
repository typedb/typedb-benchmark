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
import java.util.Collections;
import java.util.List;

public class WordData {

    private static final File FIRST_NAMES_FEMALE_FILE = Paths.get("simulation/data/word/first-names-female.csv").toFile();
    private static final File FIRST_NAMES_MALE_FILE = Paths.get("simulation/data/word/first-names-male.csv").toFile();
    private static final File LAST_NAMES_FILE = Paths.get("simulation/data/word/last-names.csv").toFile();
    private static final File ADJECTIVES_FILE = Paths.get("simulation/data/word/adjectives.csv").toFile();
    private static final File NOUNS_FILE = Paths.get("simulation/data/word/nouns.csv").toFile();

    private final List<String> femaleForenames;
    private final List<String> maleForenames;
    private final List<String> surnames;
    private final List<String> adjectives;
    private final List<String> nouns;

    private WordData(List<String> femaleFirstNames, List<String> maleFirstNames,
                     List<String> lastNames, List<String> adjectives, List<String> nouns) {
        this.femaleForenames = Collections.unmodifiableList(femaleFirstNames);
        this.maleForenames = Collections.unmodifiableList(maleFirstNames);
        this.surnames = Collections.unmodifiableList(lastNames);
        this.adjectives = Collections.unmodifiableList(adjectives);
        this.nouns = Collections.unmodifiableList(nouns);
    }

    public static WordData initialise() throws IOException {
        List<String> femaleFirstNames = parse(FIRST_NAMES_FEMALE_FILE);
        List<String> maleFirstNames = parse(FIRST_NAMES_MALE_FILE);
        List<String> lastNames = parse(LAST_NAMES_FILE);
        List<String> adjectives = parse(ADJECTIVES_FILE);
        List<String> nouns = parse(NOUNS_FILE);
        return new WordData(femaleFirstNames, maleFirstNames, lastNames, adjectives, nouns);
    }

    private static List<String> parse(File csvFile) throws IOException {
        List<String> list = new ArrayList<>();
        CSVParser.parse(csvFile, StandardCharsets.UTF_8, CSVFormat.DEFAULT).forEach(csv -> list.add(csv.get(0)));
        return list;
    }

    public List<String> femaleFirstNames() {
        return femaleForenames;
    }

    public List<String> maleFirstNames() {
        return maleForenames;
    }

    public List<String> lastNames() {
        return surnames;
    }

    public List<String> adjectives() {
        return adjectives;
    }

    public List<String> nouns() {
        return nouns;
    }
}
