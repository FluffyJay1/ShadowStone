package client.ui.game.visualboardanimation.eventanimation.putcard;

import java.util.List;
import java.util.function.Function;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import client.Config;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.AnimationEmissionPropertyStrategy;
import client.ui.particle.strategy.property.ComposedEmissionPropertyStrategy;
import client.ui.particle.strategy.property.ConstantEmissionPropertyStrategy;
import client.ui.particle.strategy.property.MaxTimeEmissionPropertyStrategy;
import client.ui.particle.strategy.property.RadialVelocityEmissionPropertyStrategy;
import client.ui.particle.strategy.property.RandomAngleEmissionPropertyStrategy;
import client.ui.particle.strategy.property.RectanglePositionEmissionPropertyStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import client.ui.particle.strategy.timing.meta.DurationLimitingEmissionTimingStrategy;
import server.event.EventPutCard;

public class EventAnimationPutCardNephthys extends EventAnimationPutCard {
    private static final double CHARGE_TIME = 0.75;

    private static final Function<Float, EmissionStrategy> CHARGING_EMISSION_STRATEGY = (yscale) -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(CHARGE_TIME, new IntervalEmissionTimingStrategy((int) (5 * yscale), 0.05)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/purplecloud.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.4, 0.5)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_NORMAL, 0.6, new Vector2f(),
                            () -> new QuadraticInterpolationA(0, 0, -2),
                            () -> new LinearInterpolation(1 + Math.random() * 2, 1 + Math.random() * 3)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(100, 300)),
                    new RectanglePositionEmissionPropertyStrategy(new Vector2f(Config.WINDOW_WIDTH * 0.6f, Config.WINDOW_HEIGHT * 0.1f * yscale)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, 500))
            ))
    );

    @Override
    public void init(VisualBoard b, EventPutCard event) {
        super.init(b, event);
        this.preTime = CHARGE_TIME;
        this.postTime = 0.75;
    }

    @Override
    public void onStart() {
        super.onStart();
        float yscale = 1;
        float y = this.visualBoard.uiBoard.getBoardPosY(this.event.targetTeam);
        this.visualBoard.uiBoard.addParticleSystem(new Vector2f(0, y * Config.WINDOW_HEIGHT), UIBoard.PARTICLE_Z_BOARD, CHARGING_EMISSION_STRATEGY.apply(yscale));
    }
}
