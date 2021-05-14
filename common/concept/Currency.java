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

public class Currency {

    private final String code;
    private final String name;
    private final int hash;

    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
        this.hash = this.code.hashCode();
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Currency that = (Currency) o;
        return this.code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
