package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static <T> List<T> from(Collection<T> list, int num) { // helper
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

    /**
     * Given a list, randomly select up to num items, ensuring that the selected
     * items are different in some trait. The trait to differentiate on is
     * determined by the selector function.
     * 
     * @param <T> Type of the item
     * @param <R> Type of the trait (should have a sensible equals() implementation)
     * @param list The list to choose from
     * @param selector Function extracting the trait to bucketize on
     * @param num Number of items to choose
     * @return A list of selections, size which might be less than num
     */
    public static <T, R> List<T> havingDifferent(Collection<T> list, Function<T, R> selector, int num) {
        Map<R, List<T>> buckets = new HashMap<>();
        for (T element : list) {
            R bucket = selector.apply(element);
            List<T> relevantList = buckets.get(bucket);
            if (relevantList == null) {
                relevantList = new ArrayList<>();
                buckets.put(bucket, relevantList);
            }
            relevantList.add(element);
        }
        List<List<T>> chosenBuckets = from(buckets.values(), num);
        List<T> chosen = chosenBuckets.stream().map(SelectRandom::from).toList();
        return chosen;
    }

    /**
     * Find random positions in a list to insert into
     * @param sizeOfList The size of the list to insert into
     * @param num The number of times you want to insert
     * @return List of size num of randomly chosen indices, in order
     */
    public static List<Integer> positionsToAdd(int sizeOfList, int num) {
        List<Integer> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            ret.add((int) (Math.random() * (sizeOfList + 1)));
            sizeOfList++;
        }
        return ret;
    }

    /**
     * Round a number randomly, such that it is more likely to round towards the
     * integer it is already close to, and that the average result of the
     * roundings tends to the original number, e.g. 5.2 has an 80% chance of
     * rounding to 5, and a 20% chance of rounding to 6
     * @param x The number to round
     * @return The number rounded randomly
     */
    public static int ditherRound(double x) {
        if (Math.random() > x % 1) {
            return (int) x;
        }
        return (int) x + 1;
    }
}
