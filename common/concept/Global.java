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

import java.util.ArrayList;

public class Global implements Region {

    private final ArrayList<Continent> continents;

    public Global() {
        continents = new ArrayList<>();
    }

    public void addContinent(Continent continent) {
        continents.add(continent);
    }

    public ArrayList<Continent> continents() {
        return continents;
    }

    @Override
    public String code() {
        return "G";
    }

    @Override
    public String name() {
        return "global";
    }

    @Override
    public String tracker() {
        return "global";
    }

    @Override
    public String group() {
        return name();
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return code().hashCode();
    }
}
