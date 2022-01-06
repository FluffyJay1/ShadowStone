package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.interpolation.meta.ProductInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.particle.Particle;

import java.util.List;

// scales the position, size, velocity of the particles
public class ScaleEmissionPropertyStrategy implements EmissionPropertyStrategy {
    private double scale;

    public ScaleEmissionPropertyStrategy(double scale) {
        this.scale = scale;
    }

    @Override
    public void applyProperties(Particle p) {
        p.pos.scale((float) this.scale);
        p.vel.scale((float) this.scale);
        p.scaleInterpolation = new ProductInterpolation(List.of(p.scaleInterpolation, new ConstantInterpolation(this.scale)));
    }
}
