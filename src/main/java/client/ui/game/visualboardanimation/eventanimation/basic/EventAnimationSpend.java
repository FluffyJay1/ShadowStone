package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Card;
import server.card.CardStatus;
import server.event.EventSpend;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationSpend extends EventAnimation<EventSpend> {
    private static final Supplier<EmissionStrategy> SPEND_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> {
                        Animation anim = new Animation("res/game/manaorb.png", new Vector2f(2, 1), 0, 0);
                        anim.play = true;
                        anim.setFrameInterval(0.1 + Math.random() * 0.2);
                        return anim;
                    }),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.6)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.3, new Vector2f(0, 6000),
                            new QuadraticInterpolationB(1, 0, 0),
                            new ConstantInterpolation(1)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(200, 600)),
                    new VelocityAddEmissionPropertyStrategy(new Vector2f(0, -700))
            ))
    );

    public EventAnimationSpend() {
        super(0, 0.2);
    }

    public void onProcess() {
        Card c = this.event.source.owner;
        UICard uic = c.uiCard;
        int z = c.status.equals(CardStatus.BOARD) ? UIBoard.PARTICLE_Z_BOARD : UIBoard.PARTICLE_Z_SPECIAL;
        for (int i = 0; i < this.event.amount; i++) {
            this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), z, new ScaledEmissionStrategy(SPEND_EMISSION_STRATEGY.get(), uic.getScale()));
        }
    }
}
