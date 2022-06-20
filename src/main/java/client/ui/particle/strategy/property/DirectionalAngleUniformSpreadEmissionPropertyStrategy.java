package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

// set angle, every group of (num) particles gets evenly distributed among a certain spread
public class DirectionalAngleUniformSpreadEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final double angle;
    double spreadDeg;
    final Interpolation<Double> spreadRange; // randomly determine the spread angle per cycle
    int num; // how many particles to fit in the spread
    int i; // current particle count

    public DirectionalAngleUniformSpreadEmissionPropertyStrategy(double angle, Interpolation<Double> spreadRange, int num) {
        this.angle = angle;
        this.spreadRange = spreadRange;
        this.num = num;
        this.i = 0;
        this.spreadDeg = this.spreadRange.get(Math.random());
    }

    @Override
    public void applyProperties(Particle p) {
        double angle = this.angle;
        if (this.num > 1) {
            double additionalDeg = (((double) this.i / (this.num - 1)) - 0.5) * this.spreadDeg;
            angle += additionalDeg;
        }
        p.angle = angle;
        this.i = (this.i + 1) % this.num;
        if (this.i == 0) {
            this.spreadDeg = this.spreadRange.get(Math.random());
        }
    }
}
