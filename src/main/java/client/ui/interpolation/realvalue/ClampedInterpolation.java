package client.ui.interpolation.realvalue;

import client.ui.interpolation.Interpolation;

// maps [0, startTime) to 0, [startTime, endTime] to [0, 1], and (endTime, 1] to 1
public class ClampedInterpolation implements Interpolation<Double> {
    double startTime, endTime;

    public ClampedInterpolation(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    @Override
    public Double get(double t) {
        if (t < this.startTime) {
            return (double) 0;
        }
        if (t > this.endTime) {
            return (double) 1;
        }
        return (t - this.startTime) / (this.endTime - this.startTime);
    }
}
