package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;

public class DirectionalAngleEmissionPropertyStrategy implements EmissionPropertyStrategy {
    private final Interpolation<Double> angleRange, angleVelRange;

    public DirectionalAngleEmissionPropertyStrategy(Interpolation<Double> angleRange, Interpolation<Double> angleVelRange) {
        this.angleRange = angleRange;
        this.angleVelRange = angleVelRange;
    }

    @Override
    public void applyProperties(Particle p) {
        p.angle = this.angleRange.get(Math.random());
        p.angleVel = this.angleVelRange.get(Math.random());
    }
}
