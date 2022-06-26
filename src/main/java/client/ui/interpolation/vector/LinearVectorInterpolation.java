package client.ui.interpolation.vector;

import client.ui.interpolation.Interpolation;
import org.newdawn.slick.geom.Vector2f;

public class LinearVectorInterpolation implements Interpolation<Vector2f> {
    Vector2f start, end;

    public LinearVectorInterpolation(Vector2f start, Vector2f end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Vector2f get(double t) {
        return this.start.copy().add(this.end.copy().sub(this.start).scale((float) t));
    }
}
