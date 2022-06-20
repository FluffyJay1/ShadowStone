package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Config;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import client.ui.particle.strategy.timing.meta.DurationLimitingEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EventAnimationDamageAOEFire extends EventAnimationDamage {
    private static final double CHARGE_TIME = 0.4;

    private static final Function<Float, EmissionStrategy> CHARGING_EMISSION_STRATEGY = (yscale) -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(CHARGE_TIME, new IntervalEmissionTimingStrategy((int) (5 * yscale), 0.05)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.2, 0.4)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0.6, new Vector2f(0, -300),
                            () -> new QuadraticInterpolationA(0, 0, -1.5),
                            () -> new LinearInterpolation(1 + Math.random() * 3, 3 + Math.random() * 3)
                    ),
                    new RectanglePositionEmissionPropertyStrategy(new Vector2f(Config.WINDOW_WIDTH * 0.6f, Config.WINDOW_HEIGHT * 0.3f * yscale)),
                    new DirectionalVelocityEmissionPropertyStrategy(new Vector2f(0, -1), new LinearInterpolation(100, 200)),
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
                            () -> new QuadraticInterpolationB(1, 0, -2),
                            () -> new QuadraticInterpolationB(1, 3, 4)
                    ),
                    new CirclePositionEmissionPropertyStrategy(20),
                    new RadialVelocityEmissionPropertyStrategy(new QuadraticInterpolationB(0, 350, 0)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-100, 100))
            ))
    );

    private static final double INCLUDE_LEADER_OFFSET = 0.2;

    final int team;
    final boolean includeLeader;

    public EventAnimationDamageAOEFire(int team, boolean includeLeader) {
        super(CHARGE_TIME, false);
        this.team = team;
        this.includeLeader = includeLeader;
    }

    @Override
    public void onStart() {
        float y = 0;
        float yscale = 1;
        if (this.team == 0) {
            yscale *= 2;
        } else  {
            y = this.visualBoard.uiBoard.getBoardPosY(this.team);
            if (this.includeLeader) {
                y += INCLUDE_LEADER_OFFSET * this.team * this.visualBoard.getLocalteam();
            }
        }
        if (this.includeLeader) {
            yscale *= 1.5;
        }
        this.visualBoard.uiBoard.addParticleSystem(new Vector2f(0, y * Config.WINDOW_HEIGHT / 2), UIBoard.PARTICLE_Z_BOARD, CHARGING_EMISSION_STRATEGY.apply(yscale));
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(),UIBoard.PARTICLE_Z_BOARD, EXPLOSION_EMISSION_STRATEGY.get());
        }
    }

    @Override
    public String extraParamString() {
        return this.team + " " + this.includeLeader + " ";
    }

    public static EventAnimationDamageAOEFire fromExtraParams(StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        boolean includeLeader = Boolean.parseBoolean(st.nextToken());
        return new EventAnimationDamageAOEFire(team, includeLeader);
    }
}
