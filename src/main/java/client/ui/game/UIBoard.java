package client.ui.game;

import java.util.*;
import java.util.function.Supplier;

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
import server.playeraction.*;

public class UIBoard extends UIBox {
    public static final double CLICK_DISTANCE_THRESHOLD = 5;
    public static final double CARD_SCALE_DEFAULT = 1, CARD_SCALE_HAND = 0.75, CARD_SCALE_HAND_EXPAND = 1.2,
            CARD_SCALE_BOARD = 1, CARD_SCALE_ABILITY = 1.3, CARD_SCALE_TARGET = 1.15, CARD_SCALE_ATTACK = 1.3,
            CARDS_SCALE_PLAY = 2.5;
    public static final double BO_SPACING = 0.1, BO_Y_LOCAL = 0.105, BO_Y_ENEMY = -0.115;
    public static final double LEADER_Y_LOCAL = 0.38, LEADER_Y_ENEMY = -0.4;
    public static final double UNLEASHPOWER_X = 0.07, UNLEASHPOWER_Y_LOCAL = 0.29, UNLEASHPOWER_Y_ENEMY = -0.31;
    public static final double HAND_X_LOCAL = 0.19, HAND_X_ENEMY = 0.28, HAND_X_EXPAND_LOCAL = 0.175;
    public static final double HAND_Y_LOCAL = 0.38, HAND_Y_ENEMY = -0.41, HAND_Y_EXPAND_LOCAL = 0.33;
    public static final double HAND_X_SCALE_LOCAL = 0.22, HAND_X_SCALE_ENEMY = 0.26, HAND_X_SCALE_EXPAND_LOCAL = 0.36;
    public static final double CARD_PLAY_Y = 0.2, HAND_EXPAND_Y = 0.30;
    public static final int CARD_DEFAULT_Z = 0, CARD_HAND_Z = 2, CARD_BOARD_Z = 0, CARD_ABILITY_Z = 4,
            CARD_VISUALPLAYING_Z = 4, CARD_DRAGGING_Z = 3, PARTICLE_Z = 1;
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
    public UICard preSelectedCard, selectedCard, draggingCard, playingCard, visualPlayingCard, attackingMinion,
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
                                    + (float) (UICard.CARD_DIMENSIONS.y * CARD_SCALE_ABILITY * CARD_SCALE_HAND / 2)),
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
        for (UICard c : this.cards) {
            c.setVisible(true);
            switch (c.getCard().status) {
            case BOARD:
                c.draggable = false;
                if (c != this.unleashingMinion && c != this.attackingMinion) {
                    c.setZ(CARD_BOARD_Z);
                }
                c.setPos(new Vector2f((float) this.boardPosToX(c.getCard().cardpos, c.getCard().team),
                        (float) (c.getCard().team == this.b.localteam ? BO_Y_LOCAL : BO_Y_ENEMY)), 0.99);
                break;
            case LEADER:
                c.draggable = false;
                if (c != this.unleashingMinion && c != this.attackingMinion) {
                    c.setZ(CARD_BOARD_Z);
                }
                c.setPos(
                        // oh my this formatting
                        new Vector2f(0,
                                (float) (c.getCard().team == this.b.localteam ? LEADER_Y_LOCAL : LEADER_Y_ENEMY)),
                        1);
                break;
            case UNLEASHPOWER:
                c.draggable = false;
                c.setZ(CARD_DEFAULT_Z);
                c.setPos(new Vector2f((float) UNLEASHPOWER_X,
                        (float) (c.getCard().team == this.b.localteam ? UNLEASHPOWER_Y_LOCAL : UNLEASHPOWER_Y_ENEMY)),
                        1);
                break;
            case HAND:
                if (c != this.draggingCard) {
                    c.setZ(CARD_HAND_Z);
                }
                if (c.getCard().team == this.b.localteam && !this.b.disableInput) {
                    c.draggable = true;
                }
                if (c != this.playingCard && c != this.draggingCard && c != this.visualPlayingCard) {
                    // ignore the following behemoth of a statement
                    // dont bother understanding it lol
                    // if there's a bug then god have mercy
                    c.setPos(
                            new Vector2f(
                                    (float) (((c.getCard().cardpos)
                                            - (this.b.getPlayer(c.getCard().team).hand.cards.size()) / 2.)
                                            * (c.getCard().team == this.b.localteam ? (this.expandHand
                                                    ? (HAND_X_SCALE_EXPAND_LOCAL
                                                            + this.b.getPlayer(c.getCard().team).hand.cards.size()
                                                                    * 0.02)
                                                    : HAND_X_SCALE_LOCAL) : HAND_X_SCALE_ENEMY)
                                            / this.b.getPlayer(c.getCard().team).hand.cards.size()
                                            + (c.getCard().team == this.b.localteam ? (this.expandHand
                                                    ? (HAND_X_EXPAND_LOCAL
                                                            - this.b.getPlayer(c.getCard().team).hand.cards.size()
                                                                    * 0.01)
                                                    : HAND_X_LOCAL) : HAND_X_ENEMY)),
                                    (float) (c.getCard().team == this.b.localteam
                                            ? (this.expandHand ? HAND_Y_EXPAND_LOCAL : HAND_Y_LOCAL)
                                            : HAND_Y_ENEMY)),
                            0.99);
                    c.setScale(c.getCard().team == this.b.localteam
                            ? (this.expandHand ? CARD_SCALE_HAND_EXPAND : CARD_SCALE_HAND)
                            : CARD_SCALE_HAND);
                }
                break;
            default:
                c.setVisible(false);
                break;
            }
        }
        if (this.playingCard != null) {
            this.playingCard.setPos(TARGETING_CARD_POS, 0.999);
            this.playingCard.setZ(CARD_ABILITY_Z);
            this.playingCard.setScale(CARD_SCALE_ABILITY * CARD_SCALE_HAND);
        }
        if (this.unleashingMinion != null) {
            this.unleashingMinion.setZ(CARD_ABILITY_Z);
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        Target currentTargeting = Target.firstUnsetTarget(this.getCurrentTargetingTargetList());
        if (currentTargeting != null) {
            for (Card card : currentTargeting.getTargets()) {
                UICard c = card.uiCard;
                g.setColor(org.newdawn.slick.Color.red);
                g.drawRect((float) (c.getFinalPos().x - UICard.CARD_DIMENSIONS.x * c.getScale() / 2 * 0.9),
                        (float) (c.getFinalPos().y - UICard.CARD_DIMENSIONS.y * c.getScale() / 2 * 0.9),
                        (float) (UICard.CARD_DIMENSIONS.x * c.getScale() * 0.9),
                        (float) (UICard.CARD_DIMENSIONS.y * c.getScale() * 0.9));
                g.setColor(org.newdawn.slick.Color.white);
            }
        }
        if (this.draggingCard != null && this.draggingCard.getCard() instanceof BoardObject
                && this.draggingCard.getRelPos().y < CARD_PLAY_Y) {
            g.drawLine(
                    (float) (this.playBoardPosToX(
                            this.XToPlayBoardPos(this.ui.lastmousepos.x / Config.WINDOW_WIDTH - 0.5, this.b.localteam),
                            this.b.localteam) + 0.5) * Config.WINDOW_WIDTH,
                    Config.WINDOW_HEIGHT * 0.55f,
                    (float) (this.playBoardPosToX(
                            this.XToPlayBoardPos(this.ui.lastmousepos.x / Config.WINDOW_WIDTH - 0.5, 1),
                            this.b.localteam) + 0.5) * Config.WINDOW_WIDTH,
                    Config.WINDOW_HEIGHT * 0.74f);
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
            case EVENT:
                String eventstring = this.ds.readEvent();
                this.b.parseEventString(eventstring);
                if (this.skipNextEventAnimations) {
                    this.b.skipAllAnimations();
                    this.skipNextEventAnimations = false;
                }
                break;
            case BOARDRESET:
                this.resetBoard();
                this.skipNextEventAnimations = true;
                break;
            default:
                break;
            }
        }
    }

    private void resetBoard() {
        this.b.currentAnimations.clear();
        this.preSelectedCard = null;
        this.selectedCard = null;
        this.draggingCard = null;
        this.playingCard = null;
        this.visualPlayingCard = null;
        this.attackingMinion = null;
        this.unleashingMinion = null;
        this.removeChildren(this.cards);
        this.cards = new ArrayList<>();
        this.b = new VisualBoard(this, this.b.localteam);
    }

    // auxiliary function for position on board
    private double boardPosToX(int i, int team) {
        // TODO: make rotationally symmetrical
        if (team == 1) {
            return (i - (b.getBoardObjects(team).size() - 1) / 2.) * BO_SPACING;
        }
        return (i - (b.getBoardObjects(team).size() - 1) / 2.) * BO_SPACING;
    }

    private int XToBoardPos(double x, int team) {
        // TODO: make rotationally symmetrical
        int pos;
        if (team == 1) {
            pos = (int) ((x / BO_SPACING) + ((b.getBoardObjects(team).size() - 1) / 2.) + 0.5);
            if (pos >= b.getBoardObjects(team).size()) {
                pos = b.getBoardObjects(team).size() - 1;
            }
            if (pos < 0) {
                pos = 0;
            }
        } else {
            pos = (int) ((x / BO_SPACING) + ((b.getBoardObjects(team).size() - 1) / 2.) + 0.5);
            if (pos >= b.getBoardObjects(team).size()) {
                pos = b.getBoardObjects(team).size() - 1;
            }
            if (pos < 0) {
                pos = 0;
            }
        }
        return pos;
    }

    private double playBoardPosToX(int i, int team) {
        // TODO make rotationally symmetrical
        if (team == 1) {
            return (i - 0.5 - (b.getBoardObjects(team).size() - 1) / 2.) * BO_SPACING;
        }
        return (i - 0.5 - (b.getBoardObjects(team).size() - 1) / 2.) * BO_SPACING;
    }

    private int XToPlayBoardPos(double x, int team) {
        // TODO make rotationally symmetrical
        int pos;
        if (team == 1) {
            pos = (int) ((x / BO_SPACING) + ((b.getBoardObjects(team).size() - 1) / 2.) + 1);
            if (pos > b.getBoardObjects(team).size()) {
                pos = b.getBoardObjects(team).size();
            }
            if (pos < 0) {
                pos = 0;
            }
        } else {
            pos = (int) ((x / BO_SPACING) + ((b.getBoardObjects(team).size() - 1) / 2.) + 1);
            if (pos > b.getBoardObjects(team).size()) {
                pos = b.getBoardObjects(team).size();
            }
            if (pos < 0) {
                pos = 0;
            }
        }
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

    public UICard cardAtPos(Vector2f pos) {
        for (UICard c : this.cards) {
            if (c.isVisible() && c.pointIsInHitbox(pos)) {
                return c;
            }
        }
        return null;
    }

    public List<UICard> getBoardObjects(int team) {
        List<UICard> ret = new LinkedList<>();
        for (BoardObject bo : this.b.getBoardObjects(team)) {
            ret.add(bo.uiCard);
        }
        return ret;
    }

    public List<UICard> getBoardObjects(int team, boolean leader, boolean minion, boolean amulet) {
        List<UICard> ret = new LinkedList<>();
        for (BoardObject bo : this.b.getBoardObjects(team, leader, minion, amulet)) {
            ret.add(bo.uiCard);
        }
        return ret;
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        this.mouseDownPos.set(x, y);
        this.selectedCard = null;
        this.preSelectedCard = null;
        this.expandHand = x > 800 && y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
        this.handleTargeting(null);
        this.addParticleSystem(new Vector2f(x, y), DUST_EMISSION_STRATEGY.get());
    }

    public void mousePressedCard(UICard c, int button, int x, int y) {
        this.mouseDownPos.set(x, y);
        this.selectedCard = null;
        this.preSelectedCard = null;
        this.expandHand = y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
        if (!this.handleTargeting(c)) { // if we clicked on a card
            this.preSelectedCard = c;
            switch (c.getCard().status) {
            case HAND:
                if (c.getCard().team == this.b.localteam) {
                    c.setScale(CARD_SCALE_HAND_EXPAND);
                    if (this.b.realBoard.getPlayer(this.b.localteam).canPlayCard(c.getCard().realCard)
                            && !this.b.disableInput) {
                        this.draggingCard = c;
                    }
                    this.preSelectedCard = c;
                    this.expandHand = true;
                }
                break;
            case UNLEASHPOWER:
                if (c.getCard().team == this.b.localteam) {
                    if (this.b.getPlayer(this.b.localteam).canUnleash() && !this.b.disableInput) {
                        c.setScale(CARD_SCALE_ABILITY);
                        this.draggingUnleash = true;
                        for (UICard uib : this.getBoardObjects(this.b.localteam)) {
                            if (uib.getCard() instanceof Minion
                                    && this.b.getPlayer(this.b.localteam).canUnleashCard(uib.getCard())) {
                                uib.setScale(CARD_SCALE_TARGET);
                            }
                        }
                    }
                }
                break;
            case LEADER: // TODO allow leader to attac properly
            case BOARD:
                BoardObject bo = (BoardObject) c.getCard();
                if (bo != null && bo instanceof Minion && bo.realCard.team == this.b.localteam
                        && ((Minion) bo.realCard).canAttack() && !this.b.disableInput) {
                    this.attackingMinion = c;
                    for (UICard uib : this.getBoardObjects(this.b.localteam * -1, true, true, false)) {
                        if (uib.getCard() instanceof Minion && this.attackingMinion.getMinion().realMinion()
                                .getAttackableTargets().contains(uib.getMinion().realMinion())) {
                            uib.setScale(CARD_SCALE_TARGET);
                        }
                    }
                    c.setScale(CARD_SCALE_ATTACK);
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
                    && this.attackingMinion.getMinion().realMinion().getAttackableTargets()
                            .contains(c.getMinion().realMinion())) {
                this.ds.sendPlayerAction(
                        new OrderAttackAction(this.attackingMinion.getMinion().realMinion(), c.getMinion().realMinion())
                                .toString());
                c.setScale(CARD_SCALE_BOARD);
            }
            for (UICard uib : this.getBoardObjects(this.b.localteam * -1, true, true, false)) {
                if (uib.getCard() instanceof Minion && this.attackingMinion.getMinion().realMinion()
                        .getAttackableTargets().contains(uib.getMinion().realMinion())) {
                    uib.setScale(CARD_SCALE_BOARD);
                }
            }
            this.attackingMinion.setScale(CARD_SCALE_BOARD);
            this.attackingMinion = null;
        } else if (this.draggingUnleash) { // in middle of unleashing
            // preselected card is unleashpower
            this.preSelectedCard.setScale(CARD_SCALE_DEFAULT);
            for (UICard uib : this.getBoardObjects(this.b.localteam)) {
                if (uib.getCard() instanceof Minion) {
                    uib.setScale(CARD_SCALE_BOARD);
                }
            }
            this.draggingUnleash = false;
            if (c != null && c.getCard() instanceof Minion && c.getCard().team == this.b.localteam
                    && this.b.getPlayer(this.b.localteam).canUnleashCard(c.getCard())) {
                this.selectUnleashingMinion(c);
            }
        } else if (this.draggingCard != null) { // in middle of playing card
            if (this.draggingCard.getRelPos().y < CARD_PLAY_Y
                    && this.b.getPlayer(this.b.localteam).canPlayCard(this.draggingCard.getCard())) {
                this.playingCard = this.draggingCard;
                this.selectedCard = null;
                // this.resolveNoBattlecryTarget();
                this.animateTargets(Target.firstUnsetTarget(this.getCurrentTargetingTargetList()), true);
                this.playingX = this.draggingCard.getRelPos().x;
            }
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
            this.playingCard = null;
        }
        if (this.unleashingMinion != null) {
            this.unleashingMinion.setScale(CARD_SCALE_BOARD);
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
                    for (Card targetc : visualt.get(i).getTargets()) {
                        realt.get(i).addCard(targetc.realCard);
                    }
                }
                if (this.playingCard != null) {
                    this.ds.sendPlayerAction(new PlayCardAction(this.b.realBoard.getPlayer(this.b.localteam),
                            this.playingCard.getCard().realCard, XToPlayBoardPos(this.playingX, this.b.localteam),
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
                if (nextTarget.getTargets().contains(c.getCard())) {
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
        c.setScale(CARD_SCALE_ABILITY * CARD_SCALE_BOARD);
        this.finishTargeting();
    }

    public List<UICard> getTargetableCards(Target t) {
        List<UICard> list = new LinkedList<>();
        if (t == null) {
            return list;
        }
        for (Card c : this.b.getTargetableCards(t)) {
            list.add(c.uiCard);
        }
        return list;
    }

    public void animateTargets(Target t, boolean activate) {
        if (t != null) {
            List<UICard> tc = this.getTargetableCards(t);
            for (UICard c : tc) {
                c.setScale(activate ? CARD_SCALE_TARGET
                        : (c.getCard().status.equals(CardStatus.HAND) ? CARD_SCALE_HAND : CARD_SCALE_BOARD));
                if (c.getCard().status.equals(CardStatus.LEADER) && activate) {
                    this.expandHand = false;
                }
            }
        }
    }

    public ParticleSystem addParticleSystem(Vector2f absPos, EmissionStrategy es) {
        ParticleSystem ps = new ParticleSystem(this.getUI(), this.getLocalPosOf(absPos), es);
        ps.setZ(PARTICLE_Z);
        this.addChild(ps);
        return ps;
    }
}
