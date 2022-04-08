package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

// add velocity in a direction, with a random spread
public class DirectionalVelocitySpreadEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final Vector2f direction;
    double spreadRad; // total spread in radians
    final Interpolation<Double> speedRange;

    public DirectionalVelocitySpreadEmissionPropertyStrategy(Vector2f direction, double spreadRad, Interpolation<Double> speedRange) {
        this.direction = direction.copy().normalise();
        this.spreadRad = spreadRad;
        this.speedRange = speedRange;
    }

    @Override
    public void applyProperties(Particle p) {
        double directionRad = Math.atan2(this.direction.y, this.direction.x) + (Math.random() - 0.5) * this.spreadRad;
        Vector2f diff = new Vector2f((float) Math.cos(directionRad), (float) Math.sin(directionRad)).scale(this.speedRange.get(Math.random()).floatValue());
        p.vel.add(diff);
    }
}
