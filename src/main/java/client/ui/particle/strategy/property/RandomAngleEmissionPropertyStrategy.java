package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;

// apply a random angle to the particle, as well as a random angle velocity
public class RandomAngleEmissionPropertyStrategy implements EmissionPropertyStrategy {
    private final Interpolation<Double> angleVelRange;
    public RandomAngleEmissionPropertyStrategy(Interpolation<Double> angleVelRange) {
        this.angleVelRange = angleVelRange;
    }

    @Override
    public void applyProperties(Particle p) {
        p.angle = Math.random() * 360;
        p.angleVel = angleVelRange.get(Math.random());
    }
}
