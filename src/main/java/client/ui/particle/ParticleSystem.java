package client.ui.particle;

import client.ui.Animation;
import client.ui.UI;
import client.ui.UIElement;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.EmissionPropertyStrategy;
import client.ui.particle.strategy.timing.EmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class ParticleSystem extends UIElement {
    private static final int MAX_PARTICLES_PER_FRAME = 20; // just in case
    private final List<Particle> particles;
    private final EmissionTimingStrategy timingStrategy;
    private final EmissionPropertyStrategy propertyStrategy;
    private double nextEmission;

    public ParticleSystem(UI ui, Vector2f pos, EmissionStrategy strategy) {
        super(ui, pos, "");
        this.timingStrategy = strategy.getTimingStrategy();
        this.propertyStrategy = strategy.getPropertyStrategy();
        this.particles = new ArrayList<>();
        this.nextEmission = 0;
        this.ignorehitbox = true;
        this.updateEmission(0);
    }

    @Override
    public double getWidth(boolean margin) {
        return 0;
    }

    @Override
    public double getHeight(boolean margin) {
        return 0;
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.updateEmission(frametime);
        for (Iterator<Particle> ip = this.particles.iterator(); ip.hasNext();) {
            Particle p = ip.next();
            p.update(frametime);
            if (p.normalizedTime() >= 1) {
                ip.remove();
            }
        }
        if (this.timingStrategy.isFinished() && this.particles.isEmpty()) {
            // kekbye
            this.removeParent();
        }
    }

    private void updateEmission(double frametime) {
        this.nextEmission -= frametime;
        int emittedThisFrame = 0;
        while (!this.timingStrategy.isFinished() && this.nextEmission <= 0 && emittedThisFrame < MAX_PARTICLES_PER_FRAME) {
            this.emit();
            this.nextEmission += this.timingStrategy.getNextEmissionTime();
            emittedThisFrame++;
        }
    }

    private void emit() {
        Particle p = new Particle();
        this.propertyStrategy.applyProperties(p);
        this.particles.add(p);
    }

    @Override
    public void draw(Graphics g) {
        if (!this.getHide()) {
            for (Particle p : this.particles) {
                p.draw(g, this.getFinalPos());
            }
            this.drawChildren(g); // i guess
        }
    }
}
