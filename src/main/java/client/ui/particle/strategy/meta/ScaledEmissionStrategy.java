package client.ui.particle.strategy.meta;

import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.ComposedEmissionPropertyStrategy;
import client.ui.particle.strategy.property.ScaleEmissionPropertyStrategy;

import java.util.List;

public class ScaledEmissionStrategy extends EmissionStrategy {
    public ScaledEmissionStrategy(EmissionStrategy strategy, double scale) {
        super(strategy.getTimingStrategy(), new ComposedEmissionPropertyStrategy(List.of(strategy.getPropertyStrategy(), new ScaleEmissionPropertyStrategy(scale))));
    }
}
