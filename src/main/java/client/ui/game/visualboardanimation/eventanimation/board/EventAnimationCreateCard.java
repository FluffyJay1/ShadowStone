package client.ui.game.visualboardanimation.eventanimation.board;

import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.particle.ParticleSystem;
import client.ui.particle.ParticleSystemCommon;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Card;
import server.card.CardStatus;
import server.event.EventCreateCard;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationCreateCard extends EventAnimation<EventCreateCard> {
    private static final int EDGE_PADDING = 1;
    private static final float PRE_FAN_WIDTH = 0.05f;
    private static final float ENTRANCE_OFFSET_Y = 0.05f;

    private static final Supplier<EmissionStrategy> DUST_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(10),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/dust.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.7)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.2, new Vector2f(0, 700),
                            new LinearInterpolation(0.9, 0),
                            new QuadraticInterpolationA(4, 0, -4)
                    ),
                    new CirclePositionEmissionPropertyStrategy(25),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 550)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );

    private UICard firstFailedOnBoard;
    private int firstFailedPos;
    private ParticleSystem banishParticles;
    public EventAnimationCreateCard() {
        super(0, 0);
    }

    @Override
    public void init(VisualBoard b, EventCreateCard event) {
        super.init(b, event);
        switch (event.status) {
            case BOARD -> {
                this.postTime = 0.25;
            }
            case HAND, DECK -> {
                this.preTime = 0.5;
                this.postTime = 0.25;
                this.scheduleAnimation(true, 0.4, this::fanCards);
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
                uic.setZ(UIBoard.CARD_VISUALPLAYING_Z);
                // fan the cards between [-PRE_FAN_WIDTH/2, PRE_FAN_WIDTH/2]
                float fanX = PRE_FAN_WIDTH * (float) ((1 - this.visualBoard.uiBoard.getWidthInRel(uic.getWidth(false)))
                        * (i + 0.5f - this.event.cards.size() / 2.)) / (this.event.cards.size() - 1 + EDGE_PADDING * 2);
                uic.setPos(new Vector2f(0, ENTRANCE_OFFSET_Y), 1);
                uic.setPos(new Vector2f(fanX, 0), 0.999);
            }
        }
    }

    private void fanCards() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            Card c = this.event.cards.get(i);
            UICard uic = c.uiCard;
            // fan the cards between [-0.5, 0.5]
            float fanX = (float) ((1 - this.visualBoard.uiBoard.getWidthInRel(uic.getWidth(false)))
                    * (i + 0.5f - this.event.cards.size() / 2.)) / (this.event.cards.size() - 1 + EDGE_PADDING * 2);
            uic.setPos(new Vector2f(fanX, 0), 0.9999);
        }
    }

    @Override
    public void onProcess() {
        if (this.event.status.equals(CardStatus.HAND)) {
            for (int i = 0; i < this.event.cards.size(); i++) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                if (!this.event.successful.get(i)) {
                    // thanos snap the unsuccessful cards
                    EmissionStrategy strategy = new ScaledEmissionStrategy(ParticleSystemCommon.DESTROY.get(), uic.getScale());
                    this.visualBoard.uiBoard.addParticleSystem(uic.getAbsPos(), strategy);
                    this.visualBoard.uiBoard.removeUICard(uic);
                }
                this.stopUsingCardInAnimation(uic);
            }
        }
        if (this.event.status.equals(CardStatus.BOARD)) {
            boolean seenFirstFailed = false;
            for (int i = 0; i < this.event.cards.size(); i++) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                this.useCardInAnimation(uic);
                if (this.event.successful.get(i)) {
                    Vector2f destPos = this.visualBoard.uiBoard.getBoardPosFor(c.getIndex(), c.team, this.visualBoard.getPlayer(c.team).getPlayArea().size());
                    uic.setPos(destPos.copy().add(new Vector2f(0, ENTRANCE_OFFSET_Y)), 1);
                    uic.setPos(destPos, 0.999);
                    Vector2f localPosOfRel = this.visualBoard.uiBoard.getLocalPosOfRel(destPos);
                    Vector2f absPosOfLocal = this.visualBoard.uiBoard.getAbsPosOfLocal(localPosOfRel);
                    this.visualBoard.uiBoard.addParticleSystem(absPosOfLocal, DUST_EMISSION_STRATEGY.get());
                } else if (!seenFirstFailed) {
                    this.firstFailedOnBoard = uic;
                    uic.setVisible(false);
                    this.firstFailedPos = this.event.cardpos.get(i);
                    seenFirstFailed = true;
                    if (i == 0) {
                        // no success, no delay
                        this.postTime = 0.5;
                        this.showFailedSummon();
                    } else {
                        this.postTime = 0.75; // lol changing postTime in the middle of animating
                        this.scheduleAnimation(false, 0.33, this::showFailedSummon);
                    }
                } else {
                    this.visualBoard.uiBoard.removeUICard(uic);
                }
            }
        }
    }

    private void showFailedSummon() {
        if (this.firstFailedOnBoard != null) {
            this.firstFailedOnBoard.setVisible(true);
            Vector2f destPos = this.visualBoard.uiBoard.getBoardPosFor(this.firstFailedPos, this.event.team, this.visualBoard.getPlayer(this.event.team).getPlayArea().size() + 1);
            this.firstFailedOnBoard.setPos(destPos.copy().add(new Vector2f(0, ENTRANCE_OFFSET_Y)), 1);
            this.firstFailedOnBoard.setPos(destPos, 0.99);
            Vector2f localPosOfRel = this.visualBoard.uiBoard.getLocalPosOfRel(destPos);
            Vector2f absPosOfLocal = this.visualBoard.uiBoard.getAbsPosOfLocal(localPosOfRel);
            this.banishParticles = this.visualBoard.uiBoard.addParticleSystem(absPosOfLocal, ParticleSystemCommon.BANISH.get());
            this.banishParticles.followElement(this.firstFailedOnBoard, 0.99);
        }
    }

    @Override
    public void onFinish() {
        if (this.firstFailedOnBoard != null) {
            if (this.banishParticles != null) {
                this.banishParticles.kill();
                this.banishParticles.stopFollowing();
            }
            this.visualBoard.uiBoard.removeUICard(this.firstFailedOnBoard);
        }
    }

    @Override
    public void draw(Graphics g) {

    }
}
