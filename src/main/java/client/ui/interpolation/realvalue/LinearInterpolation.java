package client.ui.interpolation.realvalue;

import client.ui.interpolation.Interpolation;

public class LinearInterpolation implements Interpolation<Double> {
    double start, end;
    public LinearInterpolation(double start, double end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Double get(double t) {
        return this.start + t * (this.end - this.start);
    }
}
