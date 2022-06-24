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

import java.time.LocalDateTime;
import java.util.Objects;

public class Marriage {

    private final Person wife;
    private final Person husband;
    private final String licence;
    private final LocalDateTime date;

    public Marriage(Person wife, Person husband, String licence, LocalDateTime date) {
        this.wife = wife;
        this.husband = husband;
        this.licence = licence;
        this.date = date;
    }

    public String licence() {
        return licence;
    }

    public Person wife() {
        return wife;
    }

    public Person husband() {
        return husband;
    }

    public LocalDateTime date() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Marriage marriage = (Marriage) o;
        return wife.equals(marriage.wife) &&
                husband.equals(marriage.husband) &&
                licence.equals(marriage.licence) &&
                date.equals(marriage.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wife, husband, licence, date);
    }
}
