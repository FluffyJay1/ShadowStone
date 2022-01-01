package client.ui.interpolation;

// buckle up boys it's about to get functional
public interface Interpolation<T> {
    // t is normalized to [0, 1]
    T get(double t);
}
