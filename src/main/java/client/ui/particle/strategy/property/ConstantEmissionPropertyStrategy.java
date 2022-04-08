package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

import java.util.function.Supplier;

// set the properties on the particle that are likely to be constant
public class ConstantEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final int drawMode;
    final double velscale;
    final Vector2f accel;
    final Supplier<Interpolation<Double>> opacityInterpolationSupplier;
    final Supplier<Interpolation<Double>> scaleInterpolationSupplier;

    public ConstantEmissionPropertyStrategy(int drawMode, double velscale, Vector2f accel,
                                            Supplier<Interpolation<Double>> opacityInterpolationSupplier,
                                            Supplier<Interpolation<Double>> scaleInterpolationSupplier) {
        this.drawMode = drawMode;
        this.velscale = velscale;
        this.accel = accel;
        this.opacityInterpolationSupplier = opacityInterpolationSupplier;
        this.scaleInterpolationSupplier = scaleInterpolationSupplier;
    }

    @Override
    public void applyProperties(Particle p) {
        p.drawMode = drawMode;
        p.velscale = this.velscale;
        p.accel = accel.copy();
        p.opacityInterpolation = this.opacityInterpolationSupplier.get();
        p.scaleInterpolation = this.scaleInterpolationSupplier.get();
    }
}
