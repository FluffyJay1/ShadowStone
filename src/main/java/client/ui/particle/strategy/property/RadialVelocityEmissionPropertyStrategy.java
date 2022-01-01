package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

// set velocity with direction away from (0, 0), or randomly if at (0, 0)
// depends on current position, i.e. ordering of strategies matters
public class RadialVelocityEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final Interpolation<Double> speedRange;
    public RadialVelocityEmissionPropertyStrategy(Interpolation<Double> speedRange) {
        this.speedRange = speedRange;
    }

    @Override
    public void applyProperties(Particle p) {
        Vector2f direction;
        if (p.pos.lengthSquared() == 0) {
            double theta = Math.random() * Math.PI * 2;
            direction = new Vector2f((float) Math.cos(theta), (float) Math.sin(theta));
        } else {
            direction = p.pos.copy().normalise();
        }
        p.vel.set(direction.scale(speedRange.get(Math.random()).floatValue()));
    }
}
