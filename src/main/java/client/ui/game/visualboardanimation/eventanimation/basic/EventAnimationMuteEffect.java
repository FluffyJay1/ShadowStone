package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.*;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Card;
import server.card.CardStatus;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.event.EventMuteEffect;
import server.event.EventSetEffectStats;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class EventAnimationMuteEffect extends EventAnimation<EventMuteEffect> {
    private static final Supplier<EmissionStrategy> MUTE_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/board/mute.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(1)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0, new Vector2f(),
                            () -> new SequentialInterpolation<>(
                                    List.of(new LinearInterpolation(0, 1), new QuadraticInterpolationB(1, 0, 0)),
                                    List.of(0.2, 0.8)
                            ),
                            () -> new SequentialInterpolation<>(
                                    List.of(new ComposedInterpolation<>(new SpringInterpolation(2), new LinearInterpolation(3, 1)), new ConstantInterpolation(1)),
                                    List.of(0.2, 0.8)
                            )
                    ),
                    new RandomAngleEmissionPropertyStrategy(new ConstantInterpolation(100))
            ))
    );

    public EventAnimationMuteEffect() {
        super(0.1, 0.2);
    }

    @Override
    public boolean shouldAnimate() {
        return IntStream.range(0, this.event.cards.size()).anyMatch(this::shouldAnimate);
    }

    private boolean shouldAnimate(int i) {
        Card c = this.event.cards.get(i);
        return c.isVisibleTo(this.visualBoard.getLocalteam());
    }

    @Override
    public void onStart() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            if (this.shouldAnimate(i)) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                int z = c.status.equals(CardStatus.BOARD) ? UIBoard.PARTICLE_Z_BOARD : UIBoard.PARTICLE_Z_SPECIAL;
                this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), z, new ScaledEmissionStrategy(MUTE_EMISSION_STRATEGY.get(), uic.getScale()));
            }
        }
    }
}
