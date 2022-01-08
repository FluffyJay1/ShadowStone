package client.ui.particle.strategy.property;

import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

public class VelocityAddEmissionPropertyStrategy implements EmissionPropertyStrategy {
    private Vector2f add;
    public VelocityAddEmissionPropertyStrategy(Vector2f velocityToAdd) {
        this.add = velocityToAdd;
    }

    @Override
    public void applyProperties(Particle p) {
        p.vel.add(this.add);
    }
}
