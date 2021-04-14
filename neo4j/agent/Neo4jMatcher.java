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

package grakn.benchmark.neo4j.agent;

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.agent.MarriageAgent;
import grakn.benchmark.simulation.common.GeoData;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.benchmark.neo4j.agent.Labels.GENDER;
import static grakn.benchmark.neo4j.agent.Labels.LOCATION_NAME;
import static java.util.stream.Collectors.toList;

public class Neo4jMatcher {

    public static List<String> matchBirthsInCity(Neo4jTransaction tx, GeoData.City city, LocalDateTime today) {
        String query = "MATCH (city:City {locationName: $locationName}),\n" +
                "(child:Person {dateOfBirth: $dateOfBirth})-[:BORN_IN]->(city)\n" +
                "RETURN child.email";
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("dateOfBirth", today);
            put("locationName", city.name());
        }};
        return tx.sortedExecute(new Query(query, parameters), "child.email", null);
    }

    public static List<String> matchCitiesInContinent(Neo4jTransaction tx, GeoData.City city) {
        String query = "MATCH (city:City)-[:LOCATED_IN*2]->(continent:Continent {locationName: $continentName})\n" +
                "WHERE NOT city.locationName = $cityName\n" +
                "RETURN city.locationName";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("continentName", city.country().continent().name());
            put("cityName", city.name());
        }};
        return tx.sortedExecute(new Query(query, parameters), "city.locationName", null);
    }

    public static List<Long> matchCompaniesInContinent(Neo4jTransaction tx, GeoData.Continent continent) {
        String query = "MATCH (company:Company)-[:INCORPORATED_IN]->(country:Country)-[:LOCATED_IN]->(continent:Continent {locationName: $continentName})\n" +
                "RETURN company.companyNumber";
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("continentName", continent.name());
        }};
        return tx.sortedExecute(new Query(query, parameters), "company.companyNumber", null);
    }

    public static List<Long> matchCompaniesInCountry(Neo4jTransaction tx, GeoData.Country country, int numCompanies) {
        String query = "MATCH (company:Company)-[:INCORPORATED_IN]->(country:Country {locationName: $countryName})\n" +
                "RETURN company.companyNumber";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("countryName", country.name());
        }};
        return tx.sortedExecute(new Query(query, parameters), "company.companyNumber", numCompanies);
    }

    public static List<HashMap<MarriageAgent.SpouseType, String>> matchMarriedCouple(Neo4jTransaction tx, GeoData.City city, LocalDateTime today) {
        String query = "MATCH (city:City {locationName: $locationName}),\n" +
                "(husband:Person)-[marriage:MARRIED_TO {locationName: city.locationName}]->(wife:Person)\n" +
                "WHERE NOT (wife)<-[:CHILD_OF]-(:Person)-[:CHILD_OF]->(husband)\n" +
                "RETURN husband.email, wife.email\n" +
                "ORDER BY marriage.id ASC\n";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("locationName", city.name());
        }};
        List<Record> records = tx.execute(new Query(query, parameters));
        return records.stream().map(Record::asMap).map(r -> new HashMap<MarriageAgent.SpouseType, String>() {{
            put(MarriageAgent.SpouseType.WIFE, r.get("wife.email").toString());
            put(MarriageAgent.SpouseType.HUSBAND, r.get("husband.email").toString());
        }}).collect(toList());
    }

    public static List<Long> matchProductsInContinent(Neo4jTransaction tx, GeoData.Continent continent) {
        String query = "MATCH (continent:Continent {locationName: $continentName}),\n" +
                "(product:Product)-[:PRODUCED_IN]->(continent)\n" +
                "RETURN product.barcode";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("continentName", continent.name());
        }};
        return tx.sortedExecute(new Query(query, parameters), "product.barcode", null);
    }

    public static List<String> matchResidentsInCity(Neo4jTransaction tx, GeoData.City city, int numResidents, LocalDateTime earliestDate) {
        String query = "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $locationName})\n" +
                "WHERE NOT (person)-[:RELOCATED_TO]->()\n" +
                "AND datetime(person.dateOfBirth) <= datetime($earliestDate)\n" +
                "RETURN person.email AS email\n" +
                "UNION\n" +
                "MATCH (person:Person)-[relocatedTo:RELOCATED_TO]->(city:City)\n" +
                "WHERE datetime(relocatedTo.relocationDate) <= datetime($earliestDate)\n" +
                "WITH person, city, relocatedTo.relocationDate AS relocDate\n" +
                "ORDER BY relocDate DESC\n" +
                "WITH person.email AS email, collect(relocDate)[0] AS lastRelocDate, collect(city)[0] as lastCity\n" +
                "WHERE lastCity.locationName = $locationName\n" +
                "RETURN email;";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("locationName", city.name());
            put("earliestDate", earliestDate);
        }};
        return tx.sortedExecute(new Query(query, parameters), "email", numResidents);
    }

    public static List<String> matchUnmarriedPeopleInCity(Neo4jTransaction tx, GeoData.City city, String gender, LocalDateTime dobOfAdults) {
        String query = "MATCH (person:Person {gender: $gender})-[:BORN_IN]->(city:City {locationName: $locationName})\n" +
                "WHERE NOT (person)-[:RELOCATED_TO]->()\n" +
                "AND datetime(person.dateOfBirth) <= datetime($dobOfAdults)\n" +
                "AND NOT (person)-[:MARRIED_TO]-()\n" +
                "RETURN person.email AS email\n" +
                "UNION\n" +
                "MATCH (person:Person)-[relocatedTo:RELOCATED_TO]->(city:City)\n" +
                "WITH person, city, relocatedTo.relocationDate AS relocDate\n" +
                "ORDER BY relocDate DESC\n" +
                "WITH person.email AS email, collect(relocDate)[0] AS lastRelocDate, collect(city)[0] as lastCity\n" +
                "WHERE lastCity.locationName = $locationName\n" +
                "RETURN email;";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put(LOCATION_NAME, city.name());
            put(GENDER, gender);
            put("dobOfAdults", dobOfAdults);
        }};
        return tx.sortedExecute(new Query(query, parameters), "email", null);
    }
}
