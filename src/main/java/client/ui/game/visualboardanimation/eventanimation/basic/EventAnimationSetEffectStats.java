package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.*;

import client.Game;
import client.VisualBoard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.*;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationSetEffectStats extends EventAnimation<EventSetEffectStats> {
    private static final Supplier<EmissionStrategy> SET_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(4),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/game/statchange.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.4, 0.6)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0, new Vector2f(),
                            new QuadraticInterpolationB(1, 0, 0),
                            new ConstantInterpolation(1)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-1500, 1500))
            ))
    );
    public EventAnimationSetEffectStats() {
        super(0, 0); // default no animation if we're setting stats to invisible things
    }

    @Override
    public void init(VisualBoard b, EventSetEffectStats event) {
        super.init(b, event);
        for (int i = 0; i < this.event.targets.size(); i++) {
            if (this.shouldAnimate(i)) {
                this.postTime = 0.2; // if we can see, we animate
                break;
            }
        }
    }

    private boolean shouldAnimate(int i) {
        Effect e = this.event.targets.get(i);
        if (!e.owner.isVisible()) {
            return false;
        }
        // if only cost changed and the guy is on board, we don't care
        if (e.owner.status.equals(CardStatus.BOARD) && e.effectStats.equalExcept(this.event.newStats.get(i), EffectStats.COST)) {
            return false;
        }
        return true;
    }

    public void onProcess() {
        for (int i = 0; i < this.event.targets.size(); i++) {
            if (this.shouldAnimate(i)) {
                Effect e = this.event.targets.get(i);
                Card c = e.owner;
                UICard uic = c.uiCard;
                int z = c.status.equals(CardStatus.BOARD) ? UIBoard.PARTICLE_Z_BOARD : UIBoard.PARTICLE_Z_SPECIAL;
                this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), z, new ScaledEmissionStrategy(SET_EMISSION_STRATEGY.get(), uic.getScale()));
            }
        }
    }
}
