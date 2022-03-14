package client.ui.interpolation.meta;

import client.ui.interpolation.Interpolation;

import java.util.List;

// feed the output of one interpolation into the input of another
public class ComposedInterpolation implements Interpolation<Double> {
    List<Interpolation<Double>> interpolationOrder;
    public ComposedInterpolation(List<Interpolation<Double>> interpolationOrder) {
        this.interpolationOrder = interpolationOrder;
    }
    @Override
    public Double get(double t) {
        double cum = t;
        for (Interpolation<Double> interp : this.interpolationOrder) {
            cum = interp.get(cum);
        }
        return cum;
    }
}
