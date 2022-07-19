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

public enum Gender {
    MALE(true, "male"),
    FEMALE(false, "female");

    boolean isMale;
    private final String name;

    Gender(boolean isMale, String name) {
        this.isMale = isMale;
        this.name = name;
    }

    public static Gender of(boolean isMale) {
        return isMale ? MALE : FEMALE;
    }

    public static Gender of(String gender) {
        if (gender.equals(MALE.name)) return MALE;
        else if (gender.equals(FEMALE.name)) return FEMALE;
        else throw new IllegalArgumentException("Unrecognised Gender: " + gender);
    }

    public String value() {
        return name;
    }

    public boolean isMale() {
        return isMale;
    }

    public boolean isFemale() {
        return !isMale;
    }
}
