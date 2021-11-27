package client.ui.interpolation.realvalue;

import client.ui.interpolation.Interpolation;

public class QuadraticInterpolationA implements Interpolation<Double> {
    // model with a quadratic, setting the a in ax^2 + bx + c
    double a, b, c;
    public QuadraticInterpolationA(double start, double end, double a) {
        // b = (y2 - y1 - a(x^2 - x1^2)) / (x2 - x1)
        // c = y1 - ax1^2 - bx1
        this.a = a;
        this.b = (end - start - a);
        this.c = start;
    }
    @Override
    public Double get(double t) {
        return this.a * t * t + this.b * t + this.c;
    }
}
