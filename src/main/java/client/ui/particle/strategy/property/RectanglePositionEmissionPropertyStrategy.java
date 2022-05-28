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
        double rand = Math.random();
        Vector2f pos;
        if (rand < this.threshold / 2) {
            // top edge
            pos = new Vector2f((float) (this.dim.x * (2 * rand / this.threshold - 0.5)), -this.dim.y / 2);
        } else if (rand < this.threshold) {
            // bottom edge
            pos = new Vector2f((float) (this.dim.x * (2 * rand / this.threshold - 1.5)), this.dim.y / 2);
        } else if (rand < 0.5 + this.threshold / 2)  {
            // left edge
            pos = new Vector2f(-this.dim.x / 2, (float) (this.dim.y * (2 * (rand - this.threshold) / (1 - this.threshold) - 0.5)));
        } else {
            // right edge
            pos = new Vector2f(this.dim.x / 2, (float) (this.dim.y * ((2 * rand - 1 - this.threshold) / (1 - this.threshold) - 0.5)));
        }
        p.pos.add(pos);
    }
}
