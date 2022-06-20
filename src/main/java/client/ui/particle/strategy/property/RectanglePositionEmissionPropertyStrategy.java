package client.ui.particle.strategy.property;

import client.ui.particle.Particle;
import org.newdawn.slick.geom.Vector2f;

public class RectanglePositionEmissionPropertyStrategy implements EmissionPropertyStrategy {
    Vector2f dim;
    double threshold;

    public RectanglePositionEmissionPropertyStrategy(Vector2f dim) {
        this.dim = dim;
        this.threshold = dim.x / (dim.x + dim.y);
    }

    @Override
    public void applyProperties(Particle p) {
        p.pos.x += (Math.random() - 0.5) * dim.x;
        p.pos.y += (Math.random() - 0.5) * dim.y;
    }
}
