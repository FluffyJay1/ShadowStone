package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SelectRandom {
    /**
     * Given a list of items, randomly select one of them.
     *
     * @param list The list to sample
     * @param <T> Type of the item
     * @return An item from that list, randomly chosen, or null if list is empty
     */
    public static <T> T from(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        int randind = (int) (Math.random() * list.size());
        return list.get(randind);
    }

    /**
     * Given a list of items, randomly select some of them, without replacement.
     *
     * @param list The list to sample
     * @param num The number of items to sample
     * @param <T> Type of the item
     * @return A list of items from that list, randomly chosen
     */
    public static <T> List<T> from(List<T> list, int num) { // helper
        List<T> copy = new ArrayList<>(list);
        List<T> ret = new ArrayList<>(num);
        for (int i = 0; i < num && !copy.isEmpty(); i++) {
            int randind = (int) (Math.random() * copy.size());
            ret.add(copy.remove(randind));
        }
        return ret;
    }

    /**
     * Given a list, randomly choose one of them that have the highest/lowest trait.
     * @param list The list to choose from
     * @param mappingFunc Function extracting the trait to compare
     * @param reduceFunc Function deciding which trait wins
     * @param <T> Type of the item
     * @param <R> Type of the trait
     * @return A random item from the list, or null if the list was empty
     */
    public static <T, R> T oneOfWith(List<T> list, Function<T, R> mappingFunc, BinaryOperator<R> reduceFunc) {
        Optional<R> r = list.stream()
                .map(mappingFunc)
                .reduce(reduceFunc);
        if (r.isPresent()) {
            List<T> relevant = list.stream()
                    .filter(t -> mappingFunc.apply(t).equals(r.get()))
                    .collect(Collectors.toList());
            return from(relevant);
        }
        return null;
    }
}
