package client.ui.particle.strategy.property;

import client.ui.Animation;
import client.ui.particle.Particle;

import java.util.function.Supplier;

public class AnimationEmissionPropertyStrategy implements EmissionPropertyStrategy {
    final Supplier<Animation> animationSupplier;
    public AnimationEmissionPropertyStrategy(Supplier<Animation> animationSupplier) {
        this.animationSupplier = animationSupplier;
    }

    @Override
    public void applyProperties(Particle p) {
        p.animation = animationSupplier.get();
    }
}
