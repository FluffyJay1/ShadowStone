package client.ui.interpolation.meta;

import client.ui.interpolation.Interpolation;

public class ComposedInterpolation<T> implements Interpolation<T> {
    final Interpolation<Double> first;
    final Interpolation<T> second;
    public ComposedInterpolation(Interpolation<Double> first, Interpolation<T> second) {
        this.first = first;
        this.second = second;
    }
    @Override
    public T get(double t) {
        return this.second.get(this.first.get(t));
    }
}
