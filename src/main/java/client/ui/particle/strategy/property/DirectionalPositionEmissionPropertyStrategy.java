package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

public class DirectionalPositionEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final Interpolation<Double> distanceRange;
    final Vector2f direction;

    public DirectionalPositionEmissionPropertyStrategy(Vector2f direction, Interpolation<Double> distanceRange) {
        this.direction = direction.copy().normalise();
        this.distanceRange = distanceRange;
    }

    @Override
    public void applyProperties(Particle p) {
        p.pos.add(this.direction.copy().scale(this.distanceRange.get(Math.random()).floatValue()));
    }
}
