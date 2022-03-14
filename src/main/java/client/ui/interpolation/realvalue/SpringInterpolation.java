package client.ui.interpolation.realvalue;

import client.ui.interpolation.Interpolation;

// basically a snapshot of a sine function
public class SpringInterpolation implements Interpolation<Double> {
    double start, end, startRange, endRange;
    public SpringInterpolation(double springFactor) {
        // zoom in on sin(x) centered on x = 0
        // if springFactor = 0, we get linear interpolation
        // if springFactor = 1, we get sin(x) with domain [-pi/2, pi/2]
        // if springFactor = +infty, we get sin(x) with domain [-pi, pi]
        this.start = -Math.PI + (Math.PI / (1 + springFactor));
        this.end = Math.PI - (Math.PI / (1 + springFactor));
        this.startRange = Math.sin(this.start);
        this.endRange = Math.sin(this.end);
    }
    @Override
    public Double get(double t) {
        if (Math.abs(this.start - this.end) < 0.01) {
            // replace with linear interp
            return t;
        }
        double x = this.start + t * (this.end - this.start);
        return (Math.sin(x) - this.startRange) / (this.endRange - this.startRange);
    }
}
