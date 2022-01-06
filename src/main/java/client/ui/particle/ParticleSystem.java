package client.ui.particle;

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

public class ParticleSystem extends UIElement {
    private static final int MAX_PARTICLES_PER_FRAME = 20; // just in case
    private final List<Particle> particles;
    private final EmissionTimingStrategy timingStrategy;
    private final EmissionPropertyStrategy propertyStrategy;
    private double nextEmission;
    private boolean killed;

    public ParticleSystem(UI ui, Vector2f pos, EmissionStrategy strategy) {
        super(ui, pos, "");
        this.timingStrategy = strategy.getTimingStrategy();
        this.propertyStrategy = strategy.getPropertyStrategy();
        this.particles = new ArrayList<>();
        this.nextEmission = 0;
        this.ignorehitbox = true;
        this.updateEmission(0);
        this.killed = false;
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
        for (Iterator<Particle> ip = this.particles.iterator(); ip.hasNext();) {
            Particle p = ip.next();
            p.update(frametime);
            if (p.normalizedTime() >= 1) {
                ip.remove();
            }
        }
        this.updateEmission(frametime);
        if ((this.timingStrategy.isFinished() || this.killed) && this.particles.isEmpty()) {
            // kekbye
            this.removeParent();
        }
    }

    private void updateEmission(double frametime) {
        this.nextEmission -= frametime;
        int emittedThisFrame = 0;
        while (!this.timingStrategy.isFinished() && this.nextEmission <= 0 && emittedThisFrame < MAX_PARTICLES_PER_FRAME && !this.killed) {
            this.emit(-this.nextEmission);
            this.nextEmission += this.timingStrategy.getNextEmissionTime();
            emittedThisFrame++;
        }
    }

    private void emit(double lateness) {
        Particle p = new Particle();
        this.propertyStrategy.applyProperties(p);
        this.particles.add(p);
        p.update(lateness);
    }

    @Override
    public void draw(Graphics g) {
        if (this.isVisible()) {
            for (Particle p : this.particles) {
                p.draw(g, this.getAbsPos());
            }
            this.drawChildren(g); // i guess
        }
    }

    // kill: permanently stop emission, and remove once particles are gone
    public void kill() {
        this.killed = true;
    }
}
