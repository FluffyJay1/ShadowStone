package client.ui.interpolation.color;

import client.ui.interpolation.Interpolation;
import org.newdawn.slick.Color;

public class ConstantColorInterpolation implements Interpolation<Color> {
    private final Color color;
    public ConstantColorInterpolation(Color color) {
        this.color = color;
    }

    @Override
    public Color get(double t) {
        return this.color;
    }
}
