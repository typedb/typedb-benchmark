/*
 * Copyright (C) 2022 Vaticle
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

package com.vaticle.typedb.benchmark.common.concept;

import com.vaticle.typedb.benchmark.common.seed.SeedData;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class Continent implements Region {

    private final String code;
    private final String name;
    private final ArrayList<Country> countries;
    private final ArrayList<String> commonLastNames;
    private final ArrayList<String> commonFemaleFirstNames;
    private final ArrayList<String> commonMaleFirstNames;
    private final int hash;

    public Continent(String code) {
        this(code, null);
    }

    public Continent(String code, @Nullable String name) {
        this.code = code;
        this.name = name;
        this.countries = new ArrayList<>();
        this.commonLastNames = new ArrayList<>();
        this.commonFemaleFirstNames = new ArrayList<>();
        this.commonMaleFirstNames = new ArrayList<>();
        this.hash = this.code.hashCode();
    }

    public void addCountry(Country country) {
        countries.add(country);
    }

    public void addCommonLastName(String name) {
        commonLastNames.add(name);
    }

    public void addCommonFemaleFirstName(String name) {
        commonFemaleFirstNames.add(name);
    }

    public void addCommonMaleFirstName(String name) {
        commonMaleFirstNames.add(name);
    }

    public ArrayList<Country> countries() {
        return countries;
    }

    public ArrayList<String> commonLastNames() {
        return commonLastNames;
    }

    public ArrayList<String> commonFirstNames(Gender gender) {
        return gender.isMale() ? commonMaleFirstNames : commonFemaleFirstNames;
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
        return SeedData.buildTracker(name);
    }

    @Override
    public String group() {
        return this.name();
    }

    @Override
    public String toString() {
        return name + "(" + code + ")";
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
