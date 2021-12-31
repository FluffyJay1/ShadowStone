package utils;

import java.util.*;
import java.util.stream.Collectors;

// AI samples actions randomly, however some actions are probably more important than others
// this class does weighted sampling without replacement
public class WeightedSampler<T> {
    private final Random random;
    private final List<WeightedSamplerItem> items;

    public WeightedSampler() {
        this.random = new Random();
        this.items = new ArrayList<>();
    }

    public void add(T item, double weight) {
        assert (weight > 0);
        this.items.add(new WeightedSamplerItem(item, weight));
    }

    // returns an ordering from doing weighted sampling without replacement
    // see Efraimidis and Spirakis, 2006, Algorithm A for why this works
    public List<T> sample() {
        this.items.forEach(wsi -> wsi.rand = Math.pow(random.nextDouble(), 1 / wsi.weight));
        return this.items.stream()
                .sorted(Comparator.comparingDouble((WeightedSamplerItem a) -> a.rand).reversed())
                .map(wsi -> wsi.item)
                .collect(Collectors.toList());
    }

    public int size() {
        return this.items.size();
    }

    private class WeightedSamplerItem {
        T item;
        double weight;
        double rand;
        WeightedSamplerItem(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }
    }
}
