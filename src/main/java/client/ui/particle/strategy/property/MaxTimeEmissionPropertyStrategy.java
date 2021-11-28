package client.ui.particle.strategy.property;

import client.ui.interpolation.Interpolation;
import client.ui.particle.Particle;

public class MaxTimeEmissionPropertyStrategy implements EmissionPropertyStrategy {
    private Interpolation<Double> timeRange;

    public MaxTimeEmissionPropertyStrategy(Interpolation<Double> timeRange) {
        this.timeRange = timeRange;
    }

    @Override
    public void applyProperties(Particle p) {
        p.maxTime = this.timeRange.get(Math.random());
    }
}
