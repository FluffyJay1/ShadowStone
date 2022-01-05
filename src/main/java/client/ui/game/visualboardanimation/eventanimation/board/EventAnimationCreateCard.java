package client.ui.game.visualboardanimation.eventanimation.board;

import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.particle.strategy.EmissionStrategy;
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
            }
        }
    }

    @Override
    public void onStart() {
        if (this.event.status.equals(CardStatus.HAND) || this.event.status.equals(CardStatus.DECK)) {
            for (int i = 0; i < this.event.cards.size(); i++) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                uic.setScale(UIBoard.CARD_SCALE_PLAY);
                uic.setZ(UIBoard.CARD_VISUALPLAYING_Z);
                // fan the cards between [-0.5, 0.5]
                float fanX = (float) ((1 - this.visualBoard.uiBoard.getWidthInRel(uic.getWidth(false)))
                        * (i + 0.5f - this.event.cards.size() / 2.)) / (this.event.cards.size() - 1 + EDGE_PADDING * 2);
                uic.setPos(new Vector2f(0, ENTRANCE_OFFSET_Y), 1);
                uic.setPos(new Vector2f(fanX, 0), 0.999);
            }
        }
    }

    @Override
    public void onProcess() {
        if (this.event.status.equals(CardStatus.BOARD)) {
            for (int i = 0; i < this.event.cards.size(); i++) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                Vector2f destPos = this.visualBoard.uiBoard.getBoardPosFor(c.getIndex(), c.team, this.visualBoard.getPlayer(c.team).getPlayArea().size());
                uic.setPos(destPos.copy().add(new Vector2f(0, ENTRANCE_OFFSET_Y)), 1);
                Vector2f localPosOfRel = this.visualBoard.uiBoard.getLocalPosOfRel(destPos);
                Vector2f absPosOfLocal = this.visualBoard.uiBoard.getAbsPosOfLocal(localPosOfRel);
                this.visualBoard.uiBoard.addParticleSystem(absPosOfLocal, DUST_EMISSION_STRATEGY.get());
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
