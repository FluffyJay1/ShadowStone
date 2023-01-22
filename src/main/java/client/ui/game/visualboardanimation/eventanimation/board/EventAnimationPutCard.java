package client.ui.game.visualboardanimation.eventanimation.board;

import client.VisualBoard;
import client.ui.Animation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.*;

import client.ui.game.*;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.event.*;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationPutCard extends EventAnimation<EventPutCard> {
    private static final int EDGE_PADDING = 1;
    private static final float TEAM_OFFSET = 0.06f;
    private static final float DECK_ALIGN_WEIGHT = 0.9f; //0 means left, 1 means right

    private static final Supplier<EmissionStrategy> SPARKLE_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(4),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/misc/sparkle.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.2, 0.4)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.2, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(0.2, 0, 0),
                            () -> new LinearInterpolation(8, 1)
                    ),
                    new CirclePositionEmissionPropertyStrategy(50),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(100, 200)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-1000, 1000))
            ))
    );
    public EventAnimationPutCard() {
        super(0, 0);
    }

    @Override
    public boolean shouldAnimate() {
        return true;
    }

    @Override
    public void init(VisualBoard b, EventPutCard event) {
        super.init(b, event);
        if (event.cards.isEmpty()) {
            return; //lol dont even bother
        }
        switch (event.status) {
            case BOARD -> {
                this.postTime = 0.25;
            }
            case HAND, DECK -> {
                this.preTime = 0.2;
                this.postTime = 0.6;
                this.scheduleAnimation(false, 0.5, this::sendCards);
            }
        }
    }
    @Override
    public void onStart() {
        if (this.event.status.equals(CardStatus.HAND) || this.event.status.equals(CardStatus.DECK)) {
            for (int i = 0; i < this.event.cards.size(); i++) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                this.useCardInAnimation(uic);
                uic.setScale(UICard.SCALE_MOVE);
                if (this.visualBoard.mulligan && c.team == this.visualBoard.getLocalteam()) {
                    uic.setZ(UICard.Z_MULLIGAN);
                } else {
                    uic.setZ(UICard.Z_MOVE);
                }
                if (c.team != this.visualBoard.getLocalteam() * -1
                        || (this.event.status.equals(CardStatus.HAND) && this.event.targetTeam == this.visualBoard.getLocalteam())) {
                    uic.setFlippedOver(false);
                }
                // fan the cards between [-0.5, 0.5]
                float alignWeight = 0.5f;
                if (c.status.equals(CardStatus.DECK)) {
                    alignWeight = DECK_ALIGN_WEIGHT; // put cards closer to the deck
                }
                float fanX = (float) ((1 - this.visualBoard.uiBoard.getLocalWidthInRel(uic.getWidth(false)))
                        * (i + alignWeight - this.event.cards.size() / 2.)) / (this.event.cards.size() - 1 + EDGE_PADDING * 2);
                float fanY = c.team == this.visualBoard.getLocalteam() ? TEAM_OFFSET : -TEAM_OFFSET;
                uic.setPos(new Vector2f(fanX, fanY), 0.999);
            }
        }
    }

    @Override
    public void onProcess() {
        if (this.event.status.equals(CardStatus.HAND) || this.event.status.equals(CardStatus.DECK)) {
            for (int i = 0; i < this.event.cards.size(); i++) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                if (this.event.successful.get(i) && !uic.isFlippedOver()) {
                    EmissionStrategy strategy = new ScaledEmissionStrategy(SPARKLE_EMISSION_STRATEGY.get(), uic.getScale());
                    this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), UIBoard.PARTICLE_Z_SPECIAL, strategy);
                }
            }
        }
    }

    private void sendCards() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            Card c = this.event.cards.get(i);
            UICard uic = c.uiCard;
            if (this.event.successful.get(i) || !this.event.attempted.get(i)) {
                this.stopUsingCardInAnimation(uic);
            } else {
                // it's gonna get destroyed, don't let the uiboard mess with it
                uic.useInAnimation();
            }
        }
    }

    @Override
    public void onFinish() {
        // uh maybe add some particles or something
    }

    @Override
    public void draw(Graphics g) {

    }
}
