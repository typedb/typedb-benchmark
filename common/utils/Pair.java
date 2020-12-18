/*
 * Copyright (C) 2020 Grakn Labs
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

package grakn.simulation.common.utils;

import java.util.ArrayList;
import java.util.List;

public class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public static <T, U> List<Pair<T, U>> zip(List<T> firsts, List<U> seconds) {
        int size = firsts.size();
        if (size != seconds.size()) {
            throw new IllegalArgumentException("Cannot zip lists of different sizes: " + size + ", " + seconds.size());
        }

        List<Pair<T, U>> results = new ArrayList<>();

        for (int i = 0; i < size; ++i) {
            results.add(new Pair<>(firsts.get(i), seconds.get(i)));
        }

        return results;
    }
}
