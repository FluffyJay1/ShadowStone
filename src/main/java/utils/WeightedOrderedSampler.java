package utils;

import java.util.*;
import java.util.stream.Collectors;

// boring sort-by-weight sampling
public class WeightedOrderedSampler<T> implements WeightedSampler<T> {
    private final Map<T, WeightedSamplerItem> items;
    private double totalWeight;

    public WeightedOrderedSampler() {
        this.items = new HashMap<>();
    }

    public void add(T item, double weight) {
        assert (weight > 0);
        WeightedSamplerItem oldVal = this.items.put(item, new WeightedSamplerItem(item, weight));
        if (oldVal != null) {
            this.totalWeight -= oldVal.weight;
        }
        this.totalWeight += weight;
    }

    public List<T> sample() {
        return this.items.values().stream()
                .sorted(Comparator.comparingDouble((WeightedSamplerItem a) -> a.weight).reversed())
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
        WeightedSamplerItem(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }
    }
}
