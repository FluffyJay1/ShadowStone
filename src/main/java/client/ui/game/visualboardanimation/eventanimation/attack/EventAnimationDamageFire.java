package client.ui.game.visualboardanimation.eventanimation.attack;

import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;
import server.event.EventDamage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageFire extends EventAnimationDamage {
    private static final Supplier<EmissionStrategy> CHARGING_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(3, 0.05),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.2, 0.6)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0.6, new Vector2f(0, -300),
                            new LinearInterpolation(1, 0),
                            new LinearInterpolation(0.5, 1.5)
                    ),
                    new CirclePositionEmissionPropertyStrategy(75),
                    new DirectionalVelocityEmissionPropertyStrategy(new Vector2f(0, -1), new LinearInterpolation(600, 1000)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, 500))
            ))
    );
    private static final Supplier<EmissionStrategy> EXPLOSION_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(10),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.5, 0.9)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.65, new Vector2f(0, 0),
                            new QuadraticInterpolationB(1, 0, -2),
                            new QuadraticInterpolationB(1, 3, 4)
                    ),
                    new CirclePositionEmissionPropertyStrategy(20),
                    new RadialVelocityEmissionPropertyStrategy(new QuadraticInterpolationB(0, 350, 0)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-100, 100))
            ))
    );
    private List<ParticleSystem> chargingParticleSystems;

    public EventAnimationDamageFire() {
        super(0.4);
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.chargingParticleSystems = new ArrayList<>(event.m.size());
    }

    @Override
    public void onStart() {
        for (Minion m : this.event.m) {
            ParticleSystem ps = this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getAbsPos(), UIBoard.PARTICLE_Z_BOARD, CHARGING_EMISSION_STRATEGY.get());
            this.chargingParticleSystems.add(ps);
        }
    }

    @Override
    public void onProcess() {
        for (ParticleSystem ps : this.chargingParticleSystems) {
            ps.kill();
        }
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getAbsPos(),UIBoard.PARTICLE_Z_BOARD, EXPLOSION_EMISSION_STRATEGY.get());
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            // idk draw something here maybe
        } else {
            this.drawDamageNumber(g);
        }
    }
}
