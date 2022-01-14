package client.ui.game;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import client.ui.Animation;
import client.ui.game.visualboardanimation.VisualBoardAnimation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.*;
import client.ui.*;
import network.*;
import server.card.*;
import server.card.unleashpower.UnleashPower;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.playeraction.*;

public class UIBoard extends UIBox {
    public static final double CLICK_DISTANCE_THRESHOLD = 5;
    public static final double BO_SPACING = 0.1, BO_Y_LOCAL = 0.105, BO_Y_ENEMY = -0.115;
    public static final double LEADER_Y_LOCAL = 0.38, LEADER_Y_ENEMY = -0.4;
    public static final double UNLEASHPOWER_X = 0.07, UNLEASHPOWER_Y_LOCAL = 0.29, UNLEASHPOWER_Y_ENEMY = -0.31;
    public static final double HAND_X_LOCAL = 0.19, HAND_X_ENEMY = 0.28, HAND_X_EXPAND_LOCAL = 0.175;
    public static final double HAND_Y_LOCAL = 0.38, HAND_Y_ENEMY = -0.41, HAND_Y_EXPAND_LOCAL = 0.33;
    public static final double HAND_X_SCALE_LOCAL = 0.22, HAND_X_SCALE_ENEMY = 0.26, HAND_X_SCALE_EXPAND_LOCAL = 0.36;
    public static final double CARD_PLAY_Y = 0.2, HAND_EXPAND_Y = 0.30;
    public static final double DECK_X = 0.35, DECK_Y_LOCAL = 0.2, DECK_Y_ENEMY = -0.2;
    public static final int PARTICLE_Z_BOARD = 1, PARTICLE_Z_SPECIAL = 5;
    public static final Vector2f TARGETING_CARD_POS = new Vector2f(-0.4f, -0.22f);

    private static final Supplier<EmissionStrategy> DUST_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(6),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/dust.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.4)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.1, new Vector2f(0, 700),
                            new LinearInterpolation(0.4, 0),
                            new QuadraticInterpolationA(1, 0, -4)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 350)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
        );

    public VisualBoard b;
    public final DataStream ds;
    boolean expandHand = false;
    boolean draggingUnleash = false;
    boolean skipNextEventAnimations = false;
    Vector2f mouseDownPos = new Vector2f();
    final CardSelectPanel cardSelectPanel;
    final EndTurnButton endTurnButton;
    public final Text targetText;
    public final Text player1ManaText;
    public final Text player2ManaText;
    public final Text advantageText;
    public UICard preSelectedCard, selectedCard, draggingCard, playingCard, attackingMinion,
            unleashingMinion;
    // TODO: CARD PLAYING QUEUE AND BOARD POSITIONING
    double playingX;
    List<UICard> cards;

    public UIBoard(UI ui, int localteam, DataStream ds) {
        super(ui, new Vector2f(Config.WINDOW_WIDTH / 2, Config.WINDOW_HEIGHT / 2),
                new Vector2f(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT), "res/ui/uibox.png");
        this.cards = new ArrayList<>();
        this.b = new VisualBoard(this, localteam);
        this.ds = ds;
        this.cardSelectPanel = new CardSelectPanel(ui, this);
        this.cardSelectPanel.setZ(10);
        this.addChild(this.cardSelectPanel);
        this.cardSelectPanel.setVisible(false);
        this.endTurnButton = new EndTurnButton(ui, this);
        this.addChild(this.endTurnButton);
        this.targetText = new Text(ui, new Vector2f(), "Target", 400, 24, "Verdana", 30, 0, -1);
        this.targetText.setZ(999);
        this.player1ManaText = new Text(ui, new Vector2f(-0.25f, 0.34f), "Player 1 Mana", 400, 24, "Verdana", 30, 0, 0);
        this.player1ManaText.relpos = true;
        this.player1ManaText.setZ(1);
        this.player2ManaText = new Text(ui, new Vector2f(-0.25f, -0.44f), "Player 2 Mana", 400, 24, "Verdana", 30, 0,
                0);
        this.player2ManaText.relpos = true;
        this.player2ManaText.setZ(1);
        this.advantageText = new Text(ui, new Vector2f(-0.4f, -0.4f), "Advantage Text", 400, 24, "Verdana", 30, -1, -1);
        this.advantageText.relpos = true;
        this.advantageText.setZ(1);
        this.addChild(this.targetText);
        this.addChild(this.player1ManaText);
        this.addChild(this.player2ManaText);
        this.addChild(this.advantageText);
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.b.update(frametime);
        this.readDataStream();

        // handle targeting text
        this.targetText.setVisible(true);
        UICard relevantCard = this.getCurrentTargetingCard();
        Target relevantTarget = Target.firstUnsetTarget(this.getCurrentTargetingTargetList());
        if (relevantCard != null && relevantTarget != null) {
            this.targetText.setText(relevantTarget.description);
            this.targetText.setPos(
                    new Vector2f(relevantCard.getPos().x,
                            relevantCard.getPos().y
                                    + (float) (relevantCard.getHeight(false) / 2)),
                    1);
        } else {
            this.targetText.setVisible(false);
        }
        // end handling targeting text

        this.player1ManaText
                .setText(this.b.getPlayer(this.b.localteam).mana + "/" + this.b.getPlayer(this.b.localteam).maxmana);

        this.player2ManaText.setText(
                this.b.getPlayer(this.b.localteam * -1).mana + "/" + this.b.getPlayer(this.b.localteam * -1).maxmana);

        // move the cards to their respective positions
        for (int team : List.of(-1, 1)) {
            List<BoardObject> bos = this.b.getPlayer(team).getPlayArea();
            for (BoardObject bo : bos) {
                UICard uic = bo.uiCard;
                if (!uic.isBeingAnimated()) {
                    uic.draggable = false;
                    uic.setFlippedOver(false);
                    uic.setVisible(true);
                    // exclude cards that haven't been created yet
                    uic.setPos(this.getBoardPosFor(bo.getIndex(), team, bos.size()), 0.99);
                }
            }
            Leader leader = this.b.getPlayer(team).getLeader();
            if (leader != null) {
                UICard uic = leader.uiCard;
                if (!uic.isBeingAnimated()) {
                    uic.draggable = false;
                    uic.setFlippedOver(false);
                    uic.setVisible(true);
                    uic.setPos(
                            // oh my this formatting
                            new Vector2f(0,
                                    (float) (team == this.b.localteam ? LEADER_Y_LOCAL : LEADER_Y_ENEMY)),
                            1);
                }
            }
            UnleashPower up = this.b.getPlayer(team).getUnleashPower();
            if (up != null) {
                UICard uic = up.uiCard;
                if (!uic.isBeingAnimated()) {
                    uic.draggable = false;
                    uic.setFlippedOver(false);
                    uic.setVisible(true);
                    uic.setPos(new Vector2f((float) UNLEASHPOWER_X,
                                    (float) (team == this.b.localteam ? UNLEASHPOWER_Y_LOCAL : UNLEASHPOWER_Y_ENEMY)),
                            1);
                }
            }
            List<Card> hand = this.b.getPlayer(team).getHand();
            for (Card c : hand) {
                UICard uic = c.uiCard;
                if (!uic.isBeingAnimated()) {
                    if (team == this.b.localteam && !this.b.disableInput) {
                        uic.draggable = true;
                    }
                    uic.setFlippedOver(false);
                    uic.setVisible(true);
                    // ignore cards that are cards being animated in special ways
                    if (uic != this.playingCard && uic != this.draggingCard) {
                        // ignore the following behemoth of a statement
                        // dont bother understanding it lol
                        // if there's a bug then god have mercy
                        uic.setPos(
                                new Vector2f(
                                        (float) (((uic.getCard().getIndex())
                                                - (this.b.getPlayer(team).getHand().size()) / 2.)
                                                * (team == this.b.localteam ? (this.expandHand
                                                ? (HAND_X_SCALE_EXPAND_LOCAL
                                                + this.b.getPlayer(team).getHand().size()
                                                * 0.02)
                                                : HAND_X_SCALE_LOCAL) : HAND_X_SCALE_ENEMY)
                                                / this.b.getPlayer(team).getHand().size()
                                                + (team == this.b.localteam ? (this.expandHand
                                                ? (HAND_X_EXPAND_LOCAL
                                                - this.b.getPlayer(team).getHand().size()
                                                * 0.01)
                                                : HAND_X_LOCAL) : HAND_X_ENEMY)),
                                        (float) (team == this.b.localteam
                                                ? (this.expandHand ? HAND_Y_EXPAND_LOCAL : HAND_Y_LOCAL)
                                                : HAND_Y_ENEMY)),
                                0.99);
                    }
                }
            }
            List<Card> deck = this.b.getPlayer(team).getDeck();
            for (Card c : deck) {
                UICard uic = c.uiCard;
                if (!uic.isBeingAnimated()) {
                    uic.setFlippedOver(true);
                    uic.setVisible(true);
                    uic.setPos(new Vector2f((float) DECK_X, (float) (team == this.b.localteam ? DECK_Y_LOCAL : DECK_Y_ENEMY)), 0.99);
                }
            }
            List<Card> graveyard = this.b.getPlayer(team).getGraveyard();
            for (Card c : graveyard) {
                UICard uic = c.uiCard;
                uic.setVisible(false);
            }
            List<Card> banished = this.b.getPlayer(team).getBanished();
            for (Card c : banished) {
                UICard uic = c.uiCard;
                uic.setVisible(false);
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        Target currentTargeting = Target.firstUnsetTarget(this.getCurrentTargetingTargetList());
        if (currentTargeting != null) {
            for (Card card : currentTargeting.getTargetedCards()) {
                UICard c = card.uiCard;
                g.setColor(org.newdawn.slick.Color.red);
                g.drawRect((float) (c.getAbsPos().x - UICard.CARD_DIMENSIONS.x * c.getScale() / 2 * 0.9),
                        (float) (c.getAbsPos().y - UICard.CARD_DIMENSIONS.y * c.getScale() / 2 * 0.9),
                        (float) (UICard.CARD_DIMENSIONS.x * c.getScale() * 0.9),
                        (float) (UICard.CARD_DIMENSIONS.y * c.getScale() * 0.9));
                g.setColor(org.newdawn.slick.Color.white);
            }
        }
        if (this.draggingCard != null && this.draggingCard.getCard() instanceof BoardObject
                && this.draggingCard.getRelPos().y < CARD_PLAY_Y) {
            int wouldBePos = XToBoardPos(this.ui.lastmousepos.x / Config.WINDOW_WIDTH - 0.5, this.b.localteam, this.b.getPlayer(this.b.localteam).getPlayArea().size() + 1);
            float wouldBeX = (boardPosToX(wouldBePos, this.b.localteam, this.b.getPlayer(this.b.localteam).getPlayArea().size() + 1) + 0.5f) * Config.WINDOW_WIDTH;
            g.drawLine(wouldBeX, Config.WINDOW_HEIGHT * 0.55f, wouldBeX, Config.WINDOW_HEIGHT * 0.74f);
        }
        for (VisualBoardAnimation ea : this.b.currentAnimations) {
            if (ea.isStarted()) {
                ea.draw(g);
            }
        }
    }

    private void readDataStream() {
        if (this.ds.ready()) {
            MessageType mtype = this.ds.receive();
            switch (mtype) {
                case EVENT -> {
                    String eventstring = this.ds.readEvent();
                    this.b.parseEventString(eventstring);
                    if (this.skipNextEventAnimations) {
                        this.b.skipAllAnimations();
                        this.skipNextEventAnimations = false;
                    }
                }
                case BOARDRESET -> {
                    this.resetBoard();
                    this.skipNextEventAnimations = true;
                }
                default -> {
                }
            }
        }
    }

    public void onEventGroupPushed(EventGroup eg) {
        if (eg.type.equals(EventGroupType.MINIONATTACKORDER)) {
            for (Card c : eg.cards) {
                c.uiCard.setCombat(true);
            }
        }
    }

    public void onEventGroupPopped(EventGroup eg) {
        if (eg.type.equals(EventGroupType.MINIONATTACKORDER)) {
            for (Card c : eg.cards) {
                c.uiCard.setCombat(false);
            }
        }
    }

    private void resetBoard() {
        this.b.currentAnimations.clear();
        this.preSelectedCard = null;
        this.selectedCard = null;
        this.draggingCard = null;
        this.playingCard = null;
        this.attackingMinion = null;
        this.unleashingMinion = null;
        this.removeChildren(this.cards);
        this.cards = new ArrayList<>();
        this.b = new VisualBoard(this, this.b.localteam);
    }

    // auxiliary function for position on board
    private static float boardPosToX(int i, int team, int numCards) {
        if (team == 1) {
            return (float) ((i - (numCards - 1) / 2.) * BO_SPACING);
        }
        return (float) (-(i - (numCards - 1) / 2.) * BO_SPACING);
    }

    public Vector2f getBoardPosFor(int cardpos, int team, int numCards) {
        return new Vector2f(boardPosToX(cardpos, team, numCards),
                (float) (team == this.b.localteam ? BO_Y_LOCAL : BO_Y_ENEMY));
    }

    private static int XToBoardPos(double x, int team, int numCards) {
        int pos;
        if (team == 1) {
            pos = (int) ((x / BO_SPACING) + (numCards / 2.));
        } else {
            pos = (int) ((-x / BO_SPACING) + (numCards / 2.));
        }
        pos = Math.min(Math.max(pos, 0), numCards - 1);
        return pos;
    }

    public void addCard(Card c) {
        UICard uic = new UICard(this.ui, this, c);
        uic.relpos = true;
        c.uiCard = uic;
        uic.updateIconList();
        this.cards.add(uic);
        this.addChild(uic);
    }

    // only to be called by unsuccessful create
    public void removeUICard(UICard c) {
        this.cards.remove(c);
        this.removeChild(c);
    }

    public UICard cardAtPos(Vector2f pos) {
        for (UICard c : this.cards) {
            if (c.isVisible() && c.pointIsInHitbox(pos)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        this.mouseDownPos.set(x, y);
        this.selectedCard = null;
        this.preSelectedCard = null;
        this.expandHand = x > 800 && y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
        this.handleTargeting(null);
        this.addParticleSystem(new Vector2f(x, y), UIBoard.PARTICLE_Z_BOARD, DUST_EMISSION_STRATEGY.get());
    }

    public void mousePressedCard(UICard c, int button, int x, int y) {
        this.mouseDownPos.set(x, y);
        this.selectedCard = null;
        this.preSelectedCard = null;
        this.expandHand = y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
        if (!this.handleTargeting(c) && !c.isBeingAnimated()) { // if we clicked on a card
            this.preSelectedCard = c;
            switch (c.getCard().status) {
            case HAND:
                if (c.getCard().team == this.b.localteam) {
                    if (this.b.realBoard.getPlayer(this.b.localteam).canPlayCard(c.getCard().realCard)
                            && !this.b.disableInput) {
                        this.draggingCard = c;
                        c.setDragging(true);
                    }
                    this.expandHand = true;
                }
                break;
            case UNLEASHPOWER:
                if (c.getCard().team == this.b.localteam) {
                    if (this.b.getPlayer(this.b.localteam).canUnleash() && !this.b.disableInput) {
                        c.setTargeting(true);
                        this.draggingUnleash = true;
                        this.b.getMinions(this.b.localteam, false, true)
                                .filter(m -> this.b.getPlayer(this.b.localteam).canUnleashCard(m))
                                .forEach(m -> m.uiCard.setPotentialTarget(true));
                    }
                }
                break;
            case LEADER: // TODO allow leader to attac properly
            case BOARD:
                BoardObject bo = (BoardObject) c.getCard();
                if (bo != null && bo instanceof Minion && bo.realCard.team == this.b.localteam
                        && ((Minion) bo.realCard).canAttack() && !this.b.disableInput) {
                    this.attackingMinion = c;
                    this.b.getBoardObjects(this.b.localteam * -1, true, true, false, true)
                            .filter(target -> target instanceof Minion && this.attackingMinion.getMinion().realMinion().canAttack(((Minion) target).realMinion()))
                            .forEach(target -> target.uiCard.setPotentialTarget(true));
                    c.setOrderingAttack(true);
                }
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        if (this.mouseDownPos.distance(new Vector2f(x, y)) <= CLICK_DISTANCE_THRESHOLD) {
            this.selectedCard = this.preSelectedCard;
        }
        UICard c = this.cardAtPos(new Vector2f(x, y));
        if (this.attackingMinion != null) { // in middle of ordering attack
            if (c != null && (c.getCard() instanceof Minion) && c.getCard().team != this.b.localteam
                    && this.attackingMinion.getMinion().realMinion().canAttack(c.getMinion().realMinion())) {
                this.ds.sendPlayerAction(
                        new OrderAttackAction(this.attackingMinion.getMinion().realMinion(), c.getMinion().realMinion())
                                .toString());
            }
            this.b.getBoardObjects(this.b.localteam * -1, true, true, false, true)
                    .forEach(target -> target.uiCard.setPotentialTarget(false));
            this.attackingMinion.setOrderingAttack(false);
            this.attackingMinion = null;
        } else if (this.draggingUnleash) { // in middle of unleashing
            // preselected card is unleashpower
            this.preSelectedCard.setTargeting(false);
            this.b.getMinions(this.b.localteam, false, true)
                    .forEach(m -> m.uiCard.setPotentialTarget(false));
            this.draggingUnleash = false;
            if (c != null && c.getCard() instanceof Minion && c.getCard().team == this.b.localteam
                    && this.b.getPlayer(this.b.localteam).canUnleashCard(c.getCard())) {
                this.selectUnleashingMinion(c);
            }
        } else if (this.draggingCard != null) { // in middle of playing card
            if (this.draggingCard.getRelPos().y < CARD_PLAY_Y
                    && this.b.realBoard.getPlayer(this.b.localteam).canPlayCard(this.draggingCard.getCard().realCard)) {
                this.playingCard = this.draggingCard;
                this.playingCard.setTargeting(true);
                this.playingCard.setPos(TARGETING_CARD_POS, 0.999);
                this.selectedCard = null;
                // this.resolveNoBattlecryTarget();
                this.animateTargets(Target.firstUnsetTarget(this.getCurrentTargetingTargetList()), true);
                this.playingX = this.draggingCard.getRelPos().x;
            }
            this.draggingCard.setDragging(false);
            this.draggingCard = null;
        }
        this.finishTargeting();
    }

    // TODO: remove save scumming debug
    @Override
    public void keyPressed(int key, char c) {
        if (c == 'z') {
            this.ds.sendEmote("save");
        }
        if (c == 'x') {
            this.ds.sendEmote("load");
            this.advantageText.setText("KIRA QUEEN DAISAN NO BAKUDAN");
        }
        if (key == Input.KEY_SPACE) {
            System.out.println("KING CRIMSON");
            this.advantageText.setText("KING CRIMSON");
            this.b.skipAllAnimations();
        }
    }

    public void stopTargeting() {
        List<Target> currList = this.getCurrentTargetingTargetList();
        if (currList != null) {
            this.animateTargets(Target.firstUnsetTarget(currList), false);
        }
        Target.resetList(currList);
        Target.resetList(this.getCurrentTargetingRealTargetList());
        if (this.playingCard != null) {
            this.playingCard.setTargeting(false);
            this.playingCard = null;
        }
        if (this.unleashingMinion != null) {
            this.unleashingMinion.setTargeting(false);
            this.unleashingMinion = null;
        }
    }

    public UICard getCurrentTargetingCard() {
        return this.playingCard != null ? this.playingCard
                : (this.unleashingMinion != null ? this.unleashingMinion : null);
    }

    public List<Target> getCurrentTargetingTargetList() {
        return this.playingCard != null ? this.playingCard.getCard().getBattlecryTargets()
                : (this.unleashingMinion != null ? this.unleashingMinion.getMinion().getUnleashTargets() : null);
    }

    public List<Target> getCurrentTargetingRealTargetList() {
        return this.playingCard != null ? this.playingCard.getCard().realCard.getBattlecryTargets()
                : (this.unleashingMinion != null ? this.unleashingMinion.getMinion().realMinion().getUnleashTargets()
                        : null);
    }

    // attempt to resolve targeting as a player action to send to server
    public void finishTargeting() {
        List<Target> visualt = this.getCurrentTargetingTargetList();
        if (visualt != null) {
            Target t = Target.firstUnsetTarget(this.getCurrentTargetingTargetList());
            if (t == null) {
                // convert visual card's targets to real card's targets
                List<Target> realt = this.getCurrentTargetingRealTargetList();
                Target.resetList(realt);
                for (int i = 0; i < visualt.size(); i++) {
                    for (Card targetc : visualt.get(i).getTargetedCards()) {
                        realt.get(i).addCard(targetc.realCard);
                    }
                }
                if (this.playingCard != null) {
                    this.ds.sendPlayerAction(new PlayCardAction(this.b.realBoard.getPlayer(this.b.localteam),
                            this.playingCard.getCard().realCard, XToBoardPos(this.playingX, this.b.localteam, this.b.getPlayer(this.b.localteam).getPlayArea().size() + 1),
                            Target.listToString(realt)).toString());
                } else if (this.unleashingMinion != null) {
                    // unnecessary check but gets intent across
                    this.ds.sendPlayerAction(new UnleashMinionAction(this.b.realBoard.getPlayer(this.b.localteam),
                            this.unleashingMinion.getMinion().realMinion(), Target.listToString(realt)).toString());
                }
                this.stopTargeting();
            }
        }
    }

    // what happens when u try to click on a card
    public boolean handleTargeting(UICard c) {
        if (this.b.disableInput) {
            return false;
        }
        Target nextTarget = Target.firstUnsetTarget(this.getCurrentTargetingTargetList());
        Target nextRealTarget = Target.firstUnsetTarget(this.getCurrentTargetingRealTargetList());
        if (nextTarget != null) {
            if (c != null && c.getCard().realCard.alive && nextRealTarget.canTarget(c.getCard().realCard)) {
                if (nextTarget.getTargetedCards().contains(c.getCard())) {
                    nextTarget.removeCards(c.getCard());
                } else {
                    nextTarget.addCard(c.getCard());
                    // whether max targets have been selected or all selectable
                    // targets have been selected
                    if (nextTarget.isReady()) {
                        this.animateTargets(nextTarget, false);
                        this.animateTargets(Target.firstUnsetTarget(this.getCurrentTargetingTargetList()), true);
                    }
                }

                return true;
            } else { // invalid target, so stop targeting
                this.stopTargeting();
            }
        }
        return false;
    }

    public void selectUnleashingMinion(UICard c) {
        this.unleashingMinion = c;
        Target.resetList(c.getMinion().getUnleashTargets());
        Target.resetList(c.getMinion().realMinion().getUnleashTargets());
        this.animateTargets(Target.firstUnsetTarget(c.getMinion().getUnleashTargets()), true);
        c.setTargeting(true);
        this.finishTargeting();
    }

    public Stream<UICard> getTargetableCards(Target t) {
        return this.b.getTargetableCards(t).map(c -> c.uiCard);
    }

    public void animateTargets(Target t, boolean activate) {
        if (t != null) {
            List<UICard> targetableCards = this.getTargetableCards(t).collect(Collectors.toList());
            targetableCards.forEach(c -> c.setPotentialTarget(activate));
            if (activate) {
                if (targetableCards.stream().anyMatch(c -> c.getCard().status.equals(CardStatus.LEADER))) {
                    this.expandHand = false;
                } else if (targetableCards.stream().anyMatch(c -> c.getCard().status.equals(CardStatus.HAND))) {
                    this.expandHand = true;
                }
            }
        }
    }

    public ParticleSystem addParticleSystem(Vector2f absPos, int z, EmissionStrategy es) {
        ParticleSystem ps = new ParticleSystem(this.getUI(), this.getLocalPosOfAbs(absPos), es);
        ps.setZ(z);
        this.addChild(ps);
        return ps;
    }
}
