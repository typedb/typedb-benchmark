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

public class City implements Region {

    private final String name;
    private final Country country;
    private final int hash;
    private final String code;

    public City(String code) {
        this(code, null, null);
    }

    public City(String code, @Nullable String name, @Nullable Country country) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.hash = this.code.hashCode();
    }

    public Country country() {
        return country;
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
        return SeedData.buildTracker(country.tracker(), name);
    }

    @Override
    public String group() {
        return this.country.continent().name();
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        City that = (City) o;
        return this.code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
