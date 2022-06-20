package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import client.ui.particle.strategy.timing.meta.DurationLimitingEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;
import server.event.EventDamage;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageDoubleSlice extends EventAnimationDamage {
    private static final double SLICE_TIME = 0.15;
    private static final double SLICE_INTERVAL = 0.15;

    private static final Supplier<EmissionStrategy> SLICE_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(SLICE_INTERVAL * 1.5, new IntervalEmissionTimingStrategy(1,SLICE_INTERVAL)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation(Game.getImage("res/particle/attack/slicesingle.png"))),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(SLICE_TIME)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0, new Vector2f(),
                            () -> new QuadraticInterpolationA(0, 1, -1),
                            () -> new LinearInterpolation(4, 0)
                    ),
                    new DirectionalAngleUniformSpreadEmissionPropertyStrategy(0, new ConstantInterpolation(90), 2)
            ))
    );

    private static final Supplier<EmissionStrategy> SLICE_HIT_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(SLICE_INTERVAL * 1.5, new IntervalEmissionTimingStrategy(10, SLICE_INTERVAL)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation(Game.getImage("res/particle/attack/slicehit.png"))),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.3)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0, new Vector2f(),
                            () -> new LinearInterpolation(1, 0),
                            () -> new LinearInterpolation(1 + Math.random(), 2 + Math.random() * 2)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new ConstantInterpolation(0)),
                    new CirclePositionEmissionPropertyStrategy(10)
            ))
    );

    private static final Supplier<EmissionStrategy> GLOW_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(3),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation(Game.getImage("res/particle/misc/glow.png"))),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.1, 0.4)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0.04, new Vector2f(),
                            () -> new QuadraticInterpolationA(0.3, 0, -0.3),
                            () -> new LinearInterpolation(1, 3)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 1500))
            ))
    );

    public EventAnimationDamageDoubleSlice() {
        super(0.3, true);
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.scheduleAnimation(true, SLICE_INTERVAL / this.preTime, this::playHit);
    }

    @Override
    public void onStart() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(SLICE_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
        }
    }

    private void playHit() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(SLICE_HIT_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
        }
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(GLOW_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            // draw something idk
        } else {
            this.drawDamageNumber(g);
        }
    }
}
