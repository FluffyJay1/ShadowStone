package client.ui.interpolation.realvalue;

import client.ui.interpolation.Interpolation;

public class ConstantInterpolation implements Interpolation<Double> {
    double val;
    public ConstantInterpolation(double val) {
        this.val = val;
    }

    @Override
    public Double get(double t) {
        return this.val;
    }
}
