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

    public Person(String email, String firstName, String lastName, String address,
                  Gender gender, LocalDateTime birthDate) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.gender = gender;
        this.birthDate = birthDate;
        hash = Objects.hash(email, firstName, lastName, address, gender, birthDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person that = (Person) o;
        return (email.equals(that.email) &&
                firstName.equals(that.firstName) &&
                lastName.equals(that.lastName) &&
                address.equals(that.address) &&
                gender.equals(that.gender) &&
                birthDate.equals(that.birthDate));
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
