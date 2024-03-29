package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

public class DirectionalVelocityEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final Interpolation<Double> speedRange;
    final Vector2f direction;

    public DirectionalVelocityEmissionPropertyStrategy(Vector2f direction, Interpolation<Double> speedRange) {
        this.direction = direction.copy().normalise();
        this.speedRange = speedRange;
    }

    @Override
    public void applyProperties(Particle p) {
        p.vel.add(this.direction.copy().scale(speedRange.get(Math.random()).floatValue()));
    }
}
