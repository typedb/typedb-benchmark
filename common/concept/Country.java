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

package grakn.benchmark.common.concept;

import grakn.benchmark.common.seed.SeedData;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class Country implements Region {

    private final String code;
    private final String name;
    private final Continent continent;
    private final ArrayList<Currency> currencies;
    private final ArrayList<City> cities;
    private final ArrayList<University> universities;
    private final int hash;

    public Country(String code) {
        this(code, null, null);
    }

    public Country(String code, @Nullable String name, @Nullable Continent continent) {
        this.code = code;
        this.name = name;
        this.continent = continent;
        this.currencies = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.universities = new ArrayList<>();
        this.hash = this.code.hashCode();
    }

    public void addCurrency(Currency currency) {
        currencies.add(currency);
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public void addUniversity(University university) {
        universities.add(university);
    }

    public Continent continent() {
        return continent;
    }

    public ArrayList<Currency> currencies() {
        return currencies;
    }

    public ArrayList<City> cities() {
        return cities;
    }

    public ArrayList<University> universities() {
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
        return SeedData.buildTracker(continent.tracker(), name);
    }

    @Override
    public String group() {
        return continent.name();
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Country that = (Country) o;
        return this.code.equals(that.code) && this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
