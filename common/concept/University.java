/*
 * Copyright (C) 2021 Vaticle
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

import java.util.Objects;

public class University {

    private final String name;
    private final Country country;
    private final int hash;

    public University(String name, Country country) {
        this.name = name;
        this.country = country;
        hash = Objects.hash(name, country);
    }

    public String name() {
        return name;
    }

    public Country country() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        University that = (University) o;
        return name.equals(that.name) && country.equals(that.country);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
