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

package grakn.simulation.config;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SamplingFunction {

    EVERY("every", new int[0]),
    LOG("log", new int[0]);

    private final String name;
    private final int[] acceptedArgs;

    SamplingFunction(String name, int[] acceptedArgs) {
        this.name = name;
        this.acceptedArgs = acceptedArgs;
    }

    public static SamplingFunction getByName(String name) {
        for (SamplingFunction samplingFunction : SamplingFunction.values()) {
            if(samplingFunction.getName().equals(name)) {
                return samplingFunction;
            }
        }
        throw new IllegalArgumentException("Function name not recognised");
    }

    public String getName() {
        return name;
    }

    public int[] getAcceptedArgs() {
        return acceptedArgs;
    }

    public static Function<Integer, Boolean> applyArg(SamplingFunction sampling, Integer arg) {
        switch (sampling) {
            case EVERY:
                if (arg < 1) throw new IllegalArgumentException("`every` requires an argument of 1 or greater");
                return i -> i % arg == 0;
            case LOG:
                if (arg < 2) throw new IllegalArgumentException("`log` requires a base of 2 or greater");
                // return true if the logarithm of `i` to the given base is an integer
                return i -> ((int) Math.log(i) / Math.log(arg) % 1) == 0;
            default:
                throw new IllegalArgumentException(String.format("Function not available, possible functions are %s",
                        Arrays.stream(SamplingFunction.values()).map(Enum::name).collect(Collectors.toSet())));
        }
    }
}
