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

package grakn.benchmark.grakn.agent;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.agent.MarriageAgent;
import grakn.benchmark.simulation.common.GeoData;
import graql.lang.query.GraqlMatch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.benchmark.grakn.agent.Types.BORN_IN;
import static grakn.benchmark.grakn.agent.Types.BORN_IN_CHILD;
import static grakn.benchmark.grakn.agent.Types.BORN_IN_PLACE_OF_BIRTH;
import static grakn.benchmark.grakn.agent.Types.CITY;
import static grakn.benchmark.grakn.agent.Types.COMPANY;
import static grakn.benchmark.grakn.agent.Types.COMPANY_NUMBER;
import static grakn.benchmark.grakn.agent.Types.CONTINENT;
import static grakn.benchmark.grakn.agent.Types.COUNTRY;
import static grakn.benchmark.grakn.agent.Types.DATE_OF_BIRTH;
import static grakn.benchmark.grakn.agent.Types.EMAIL;
import static grakn.benchmark.grakn.agent.Types.END_DATE;
import static grakn.benchmark.grakn.agent.Types.GENDER;
import static grakn.benchmark.grakn.agent.Types.INCORPORATION;
import static grakn.benchmark.grakn.agent.Types.INCORPORATION_INCORPORATED;
import static grakn.benchmark.grakn.agent.Types.INCORPORATION_INCORPORATING;
import static grakn.benchmark.grakn.agent.Types.LOCATES;
import static grakn.benchmark.grakn.agent.Types.LOCATES_LOCATED;
import static grakn.benchmark.grakn.agent.Types.LOCATES_LOCATION;
import static grakn.benchmark.grakn.agent.Types.LOCATION_HIERARCHY;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.MARRIAGE;
import static grakn.benchmark.grakn.agent.Types.MARRIAGE_HUSBAND;
import static grakn.benchmark.grakn.agent.Types.MARRIAGE_ID;
import static grakn.benchmark.grakn.agent.Types.MARRIAGE_WIFE;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP_PARENT;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static grakn.benchmark.grakn.agent.Types.PRODUCED_IN;
import static grakn.benchmark.grakn.agent.Types.PRODUCED_IN_CONTINENT;
import static grakn.benchmark.grakn.agent.Types.PRODUCED_IN_PRODUCT;
import static grakn.benchmark.grakn.agent.Types.PRODUCT;
import static grakn.benchmark.grakn.agent.Types.PRODUCT_BARCODE;
import static grakn.benchmark.grakn.agent.Types.RESIDENCY;
import static grakn.benchmark.grakn.agent.Types.RESIDENCY_LOCATION;
import static grakn.benchmark.grakn.agent.Types.RESIDENCY_RESIDENT;
import static grakn.benchmark.grakn.agent.Types.START_DATE;
import static graql.lang.Graql.match;
import static graql.lang.Graql.not;
import static graql.lang.Graql.var;
import static java.util.stream.Collectors.toList;

public class GraknMatcher {

    public static List<String> matchResidentsInCity(GraknTransaction tx, GeoData.City city, int numResidents, LocalDateTime earliestDate) {
        return tx.sortedExecute(match(
                var(PERSON).isa(PERSON).has(EMAIL, var(EMAIL)),
                var(CITY).isa(CITY).has(LOCATION_NAME, city.name()),
                var("r").rel(RESIDENCY_RESIDENT, PERSON).rel(RESIDENCY_LOCATION, CITY).isa(RESIDENCY).has(START_DATE, var(START_DATE)),
                not(var("r").has(END_DATE, var(END_DATE))),
                var(START_DATE).lte(earliestDate)
        ), EMAIL, numResidents);
    }

    public static List<Long> matchCompaniesInCountry(GraknTransaction tx, GeoData.Country country, int numCompanies) {
        return tx.sortedExecute(match(
                var(COUNTRY).isa(COUNTRY)
                        .has(LOCATION_NAME, country.name()),
                var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NUMBER, var(COMPANY_NUMBER)),
                var(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, var(COUNTRY))
                        .isa(INCORPORATION)
        ), COMPANY_NUMBER, numCompanies);
    }

    public static List<HashMap<MarriageAgent.SpouseType, String>> matchMarriedCouple(GraknTransaction tx, GeoData.City city) {
        GraqlMatch.Sorted marriageQuery = match(
                var(CITY).isa(CITY).has(LOCATION_NAME, city.name()),
                var("m").rel(MARRIAGE_HUSBAND, var("husband")).rel(MARRIAGE_WIFE, var("wife")).isa(MARRIAGE).has(MARRIAGE_ID, var(MARRIAGE_ID)),
                not(var("par").rel(PARENTSHIP_PARENT, "husband").rel(PARENTSHIP_PARENT, "wife").isa(PARENTSHIP)),
                var("husband").isa(PERSON).has(EMAIL, var("husband-email")),
                var("wife").isa(PERSON).has(EMAIL, var("wife-email")),
                var().rel(LOCATES_LOCATED, var("m")).rel(LOCATES_LOCATION, var(CITY)).isa(LOCATES)
        ).sort(MARRIAGE_ID);
        return tx.execute(marriageQuery)
                .stream()
                .map(a -> new HashMap<MarriageAgent.SpouseType, String>() {{
                    put(MarriageAgent.SpouseType.WIFE, a.get("wife-email").asThing().asAttribute().getValue().toString());
                    put(MarriageAgent.SpouseType.HUSBAND, a.get("husband-email").asThing().asAttribute().getValue().toString());
                }})
                .collect(toList());
    }

    public static List<String> matchUnmarriedPeople(GraknTransaction tx, GeoData.City city, String gender, LocalDateTime dobOfAdults) {
        String marriageRole;
        if (gender.equals("female")) {
            marriageRole = MARRIAGE_WIFE;
        } else if (gender.equals("male")) {
            marriageRole = MARRIAGE_HUSBAND;
        } else {
            throw new IllegalArgumentException("Gender must be male or female");
        }
        GraqlMatch query = match(
                var(PERSON).isa(PERSON).has(GENDER, gender).has(EMAIL, var(EMAIL)).has(DATE_OF_BIRTH, var(DATE_OF_BIRTH)),
                var(DATE_OF_BIRTH).lte(dobOfAdults),
                not(var("m").rel(marriageRole, var(PERSON)).isa(MARRIAGE)),
                var("r").rel(RESIDENCY_RESIDENT, var(PERSON)).rel(RESIDENCY_LOCATION, var(CITY)).isa(RESIDENCY),
                not(var("r").has(END_DATE, var(END_DATE))),
                var(CITY).isa(CITY).has(LOCATION_NAME, city.name())
        ).get(EMAIL);
        return tx.sortedExecute(query, EMAIL, null);
    }

    public static List<String> matchBirthsInCity(GraknTransaction tx, GeoData.City city, LocalDateTime today) {
        return tx.sortedExecute(match(
                var("c").isa(CITY)
                        .has(LOCATION_NAME, city.name()),
                var("child").isa(PERSON)
                        .has(EMAIL, var(EMAIL))
                        .has(DATE_OF_BIRTH, today),
                var("bi")
                        .rel(BORN_IN_PLACE_OF_BIRTH, "c")
                        .rel(BORN_IN_CHILD, "child")
                        .isa(BORN_IN)
        ), EMAIL, null);
    }

    public static List<Long> matchProductsInContinent(GraknTransaction tx, GeoData.Continent continent) {
        return tx.sortedExecute(match(
                var(CONTINENT).isa(CONTINENT).has(LOCATION_NAME, continent.name()),
                var(PRODUCT).isa(PRODUCT).has(PRODUCT_BARCODE, var(PRODUCT_BARCODE)),
                var(PRODUCED_IN).rel(PRODUCED_IN_PRODUCT, var(PRODUCT)).rel(PRODUCED_IN_CONTINENT, var(CONTINENT)).isa(PRODUCED_IN)
        ), PRODUCT_BARCODE, null);
    }

    public static List<String> matchCitiesInContinent(GraknTransaction tx, GeoData.City city) {
        return tx.sortedExecute(match(
                var(CITY).isa(CITY).has(LOCATION_NAME, var("city-name")),
                var(CONTINENT).isa(CONTINENT).has(LOCATION_NAME, city.country().continent().name()),
                var("lh1").rel(CITY).rel(CONTINENT).isa(LOCATION_HIERARCHY),
                var("city-name").neq(city.name())
        ), "city-name", null);
    }

    public static List<String> matchCompaniesInContinent(GraknTransaction tx, GeoData.Continent continent) {
        return tx.sortedExecute(match(
                var(CONTINENT).isa(CONTINENT)
                        .has(LOCATION_NAME, continent.name()),
                var(LOCATION_HIERARCHY).rel(COUNTRY).rel(CONTINENT).isa(LOCATION_HIERARCHY),
                var(COUNTRY).isa(COUNTRY),
                var(COMPANY).isa(COMPANY)
                        .has(COMPANY_NUMBER, var(COMPANY_NUMBER)),
                var(INCORPORATION)
                        .rel(INCORPORATION_INCORPORATED, var(COMPANY))
                        .rel(INCORPORATION_INCORPORATING, var(COUNTRY))
                        .isa(INCORPORATION)
        ), COMPANY_NUMBER, null);
    }
}
