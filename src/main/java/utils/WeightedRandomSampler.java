package utils;

import java.util.*;
import java.util.stream.Collectors;

// AI samples actions randomly, however some actions are probably more important than others
// this class does weighted sampling without replacement
public class WeightedRandomSampler<T> implements WeightedSampler<T> {
    private final Random random;
    private final Map<T, WeightedSamplerItem> items;
    private double totalWeight;

    public WeightedRandomSampler() {
        this.random = new Random();
        this.items = new HashMap<>();
        this.totalWeight = 0;
    }

    public void add(T item, double weight) {
        assert (weight > 0);
        WeightedSamplerItem oldVal = this.items.put(item, new WeightedSamplerItem(item, weight));
        if (oldVal != null) {
            this.totalWeight -= oldVal.weight;
        }
        this.totalWeight += weight;
    }

    // returns an ordering from doing weighted sampling without replacement
    // see Efraimidis and Spirakis, 2006, Algorithm A for why this works
    public List<T> sample() {
        this.items.values().forEach(wsi -> wsi.rand = Math.pow(random.nextDouble(), 1 / wsi.weight));
        return this.items.values().stream()
                .sorted(Comparator.comparingDouble((WeightedSamplerItem a) -> a.rand).reversed())
                .map(wsi -> wsi.item)
                .collect(Collectors.toList());
    }

    public int size() {
        return this.items.size();
    }

    @Override
    public double getTotalWeight() {
        return this.totalWeight;
    }

    @Override
    public double getWeightOfItem(T item) {
        WeightedSamplerItem mapped = this.items.get(item);
        if (mapped != null) {
            return mapped.weight;
        }
        return 0;
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
