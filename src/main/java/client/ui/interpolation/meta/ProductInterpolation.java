package client.ui.interpolation.meta;

import client.ui.interpolation.Interpolation;

import java.util.List;

// multiply a bunch of interpolations together
public class ProductInterpolation implements Interpolation<Double> {
    private final List<Interpolation<Double>> interpolationList;

    public ProductInterpolation(List<Interpolation<Double>> interpolationList) {
        this.interpolationList = interpolationList;
    }

    @Override
    public Double get(double t) {
        return this.interpolationList.stream().map(ip -> ip.get(t)).reduce(1., (a, b) -> a * b);
    }
}
