package client.ui.interpolation.vector;

import client.ui.interpolation.Interpolation;
import org.newdawn.slick.geom.Vector2f;

public class CircleInterpolation implements Interpolation<Vector2f> {
    double radius;

    public CircleInterpolation() {
        this(1);
    }

    public CircleInterpolation(double radius) {
        this.radius = radius;
    }

    @Override
    public Vector2f get(double t) {
        double rad = t * 2 * Math.PI;
        return new Vector2f((float) Math.cos(rad), (float) Math.sin(rad)).scale((float) this.radius);
    }
}
