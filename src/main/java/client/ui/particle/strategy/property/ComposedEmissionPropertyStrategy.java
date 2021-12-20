package client.ui.particle.strategy.property;

import client.ui.particle.Particle;

import java.util.List;

public class ComposedEmissionPropertyStrategy implements EmissionPropertyStrategy {
    private final List<EmissionPropertyStrategy> strategies;

    public ComposedEmissionPropertyStrategy(List<EmissionPropertyStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void applyProperties(Particle p) {
        for (EmissionPropertyStrategy s : this.strategies) {
            s.applyProperties(p);
        }
    }
}
