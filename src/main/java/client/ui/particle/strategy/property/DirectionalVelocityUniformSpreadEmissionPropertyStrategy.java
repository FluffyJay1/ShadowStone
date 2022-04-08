package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

// add velocity in a direction, every group of (num) particles gets evenly distributed among a certain spread
// i dare you to make a class with a name longer than this
public class DirectionalVelocityUniformSpreadEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final Vector2f direction;
    double spreadRad;
    final Interpolation<Double> spreadRange; // randomly determine the spread angle per cycle
    final Interpolation<Double> speedRange;
    int num; // how many particles to fit in the spread
    int i; // current particle count

    public DirectionalVelocityUniformSpreadEmissionPropertyStrategy(Vector2f direction, Interpolation<Double> spreadRange, Interpolation<Double> speedRange, int num) {
        this.direction = direction.copy().normalise();
        this.spreadRange = spreadRange;
        this.speedRange = speedRange;
        this.num = num;
        this.i = 0;
        this.spreadRad = this.spreadRange.get(Math.random());
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
        if (this.i == 0) {
            this.spreadRad = this.spreadRange.get(Math.random());
        }
    }
}
