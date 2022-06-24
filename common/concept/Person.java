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

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Person {

    private final String email;
    private final String firstName;
    private final String lastName;
    private final String address;
    private final Gender gender;
    private final LocalDateTime birthDate;
    private final int hash;

    public Person(String email) {
        this(email, null, null, null, null, null);
    }

    public Person(String email, @Nullable String firstName, @Nullable String lastName, @Nullable String address,
                  @Nullable Gender gender, @Nullable LocalDateTime birthDate) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.gender = gender;
        this.birthDate = birthDate;
        hash = Objects.hash(email, firstName, lastName, address, gender, birthDate);
    }

    public String email() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person that = (Person) o;
        return (email.equals(that.email) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(address, that.address) &&
                Objects.equals(gender, that.gender) &&
                Objects.equals(birthDate, that.birthDate));
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
