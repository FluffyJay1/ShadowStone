package client.ui.interpolation.color;

import client.ui.interpolation.Interpolation;
import org.newdawn.slick.Color;

public class LinearColorInterpolation implements Interpolation<Color> {
    private final Color from, to;
    public LinearColorInterpolation(Color from, Color to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Color get(double t) {
        return this.from.scaleCopy(1 - (float) t).addToCopy(this.to.scaleCopy((float) t));
    }
}
