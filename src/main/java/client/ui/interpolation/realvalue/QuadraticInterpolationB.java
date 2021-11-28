package client.ui.interpolation.realvalue;

import client.ui.interpolation.Interpolation;

public class QuadraticInterpolationB implements Interpolation<Double> {
    // model with a quadratic, setting the a in ax^2 + bx + c
    double a, b, c;
    public QuadraticInterpolationB(double start, double end, double b) {
        this.b = b;
        this.c = start;
        this.a = end - b - c;
    }
    @Override
    public Double get(double t) {
        return this.a * t * t + this.b * t + this.c;
    }
}
