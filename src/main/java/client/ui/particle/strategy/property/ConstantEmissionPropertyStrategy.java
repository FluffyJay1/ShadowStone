package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

// set the properties on the particle that are likely to be constant
public class ConstantEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final int drawMode;
    final double velscale;
    final Vector2f accel;
    final Interpolation<Double> opacityInterpolation;
    final Interpolation<Double> scaleInterpolation;

    public ConstantEmissionPropertyStrategy(int drawMode, double velscale, Vector2f accel,
                                            Interpolation<Double> opacityInterpolation,
                                            Interpolation<Double> scaleInterpolation) {
        this.drawMode = drawMode;
        this.velscale = velscale;
        this.accel = accel;
        this.opacityInterpolation = opacityInterpolation;
        this.scaleInterpolation = scaleInterpolation;
    }

    @Override
    public void applyProperties(Particle p) {
        p.drawMode = drawMode;
        p.velscale = this.velscale;
        p.accel = accel.copy();
        p.opacityInterpolation = this.opacityInterpolation;
        p.scaleInterpolation = this.scaleInterpolation;
    }
}
