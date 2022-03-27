package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

// add velocity in a direction, every group of (num) particles gets evenly distributed among a certain spread
// i dare you to make a class with a name longer than this
public class DirectionalVelocityUniformSpreadEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final Vector2f direction;
    double spreadRad; // total spread in radians
    final Interpolation<Double> speedRange;
    int num; // how many particles to fit in the spread
    int i; // current particle count

    public DirectionalVelocityUniformSpreadEmissionPropertyStrategy(Vector2f direction, double spreadRad, Interpolation<Double> speedRange, int num) {
        this.direction = direction.copy().normalise();
        this.spreadRad = spreadRad;
        this.speedRange = speedRange;
        this.num = num;
        this.i = 0;
    }

    @Override
    public void applyProperties(Particle p) {
        double directionRad = Math.atan2(this.direction.y, this.direction.x);
        if (this.num > 1) {
            double additionalRad = (((double) this.i / (this.num - 1)) - 0.5) * this.spreadRad;
            directionRad += additionalRad;
        }
        Vector2f diff = new Vector2f((float) Math.cos(directionRad), (float) Math.sin(directionRad)).scale(this.speedRange.get(Math.random()).floatValue());
        p.vel.add(diff);
        this.i = (this.i + 1) % this.num;
    }
}
