package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

import java.util.function.Supplier;

// set the properties on the particle that are likely to be constant
public class ConstantEmissionPropertyStrategy implements EmissionPropertyStrategy {
    double velscale, maxTime;
    Interpolation<Double> opacityInterpolation;
    Interpolation<Double> scaleInterpolation;
    Vector2f accel;

    public ConstantEmissionPropertyStrategy(double velscale, double maxTime, Vector2f accel,
                                            Interpolation<Double> opacityInterpolation,
                                            Interpolation<Double> scaleInterpolation) {
        this.velscale = velscale;
        this.maxTime = maxTime;
        this.accel = accel;
        this.opacityInterpolation = opacityInterpolation;
        this.scaleInterpolation = scaleInterpolation;
    }

    @Override
    public void applyProperties(Particle p) {
        p.velscale = this.velscale;
        p.maxTime = this.maxTime;
        p.accel = accel.copy();
        p.opacityInterpolation = this.opacityInterpolation;
        p.scaleInterpolation = this.scaleInterpolation;
    }
}
