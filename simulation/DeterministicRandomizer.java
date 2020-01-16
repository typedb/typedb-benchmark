package grakn.simulation;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DeterministicRandomizer {
    private final long seed;

    public DeterministicRandomizer(long seed) {
        this.seed = seed;
    }

    public <T> List<RandomizedElement<T>> split(List<T> items) {
        Random random = createRandom();
        return items.stream().map(i -> new RandomizedElement<>(new DeterministicRandomizer(random.nextLong()), i))
                .collect(Collectors.toList());
    }

    public Random createRandom() {
        return new Random(seed);
    }

    public static class RandomizedElement<T> {
        private final DeterministicRandomizer randomizer;
        private final T item;

        private RandomizedElement(DeterministicRandomizer randomizer, T item) {
            this.randomizer = randomizer;
            this.item = item;
        }

        public T getItem() {
            return item;
        }

        public DeterministicRandomizer getRandomizer() {
            return randomizer;
        }
    }
}
