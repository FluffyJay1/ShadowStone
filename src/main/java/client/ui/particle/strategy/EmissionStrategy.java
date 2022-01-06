package client.ui.particle.strategy;

import client.ui.particle.strategy.property.ComposedEmissionPropertyStrategy;
import client.ui.particle.strategy.property.EmissionPropertyStrategy;
import client.ui.particle.strategy.timing.EmissionTimingStrategy;

import java.util.List;

// wew design pattern abuse complete with unnecessarily long class names
// so much abstraction it's a modern art masterpiece
public class EmissionStrategy {
    private final EmissionTimingStrategy ets;
    private EmissionPropertyStrategy eps;
    public EmissionStrategy(EmissionTimingStrategy ets, EmissionPropertyStrategy eps) {
        this.ets = ets;
        this.eps = eps;
    }

    public EmissionTimingStrategy getTimingStrategy() {
        return this.ets;
    }
    public EmissionPropertyStrategy getPropertyStrategy() {
        return this.eps;
    }

    public EmissionStrategy composePropertyStrategy(EmissionPropertyStrategy other) {
        this.eps = new ComposedEmissionPropertyStrategy(List.of(this.eps, other));
        return this;
    }
}
