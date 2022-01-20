package utils;

import java.util.*;

public interface WeightedSampler<T> {
    void add(T item, double weight);
    // obtain an ordering of the items
    List<T> sample();
    int size();
    double getTotalWeight();
    double getWeightOfItem(T item);
}
