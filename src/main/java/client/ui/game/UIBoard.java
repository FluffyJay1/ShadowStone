package client.ui.game;

import java.util.*;
import java.util.function.Consumer;
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
import server.Player;
import server.card.*;
import server.card.target.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.playeraction.*;

public class UIBoard extends UIBox {
    public static final double CLICK_DISTANCE_THRESHOLD = 5;
    public static final double BO_SPACING = 0.1, BO_Y_LOCAL = 0.105, BO_Y_ENEMY = -0.115;
    public static final double LEADER_Y_LOCAL = 0.38, LEADER_Y_ENEMY = -0.4;
    public static final double UNLEASHPOWER_X = 0.07, UNLEASHPOWER_Y_LOCAL = 0.27, UNLEASHPOWER_Y_ENEMY = -0.29;
    public static final double HAND_X_LOCAL = 0.19, HAND_X_ENEMY = 0.28, HAND_X_EXPAND_LOCAL = 0.175;
    public static final double HAND_Y_LOCAL = 0.38, HAND_Y_ENEMY = -0.41, HAND_Y_EXPAND_LOCAL = 0.33;
    public static final double HAND_X_SCALE_LOCAL = 0.22, HAND_X_SCALE_ENEMY = 0.26, HAND_X_SCALE_EXPAND_LOCAL = 0.40;
    private static final float MULLIGAN_FAN_WIDTH = 0.6f, MULLIGAN_KEEP_Y = 0.2f, MULLIGAN_TOSS_Y = -0.2f;
    public static final double CARD_PLAY_Y = 0.2, HAND_EXPAND_Y = 0.30;
    public static final double DECK_X = 0.35, DECK_Y_LOCAL = 0.2, DECK_Y_ENEMY = -0.2;
    public static final int PARTICLE_Z_BOARD = 1, PARTICLE_Z_SPECIAL = 5, UI_Z_TOP = 10;
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
    final EventGroupDescriptionContainer eventGroupDescriptionContainer;
    final CardSelectPanel cardSelectPanel;
    final EndTurnButton endTurnButton;
    public final Text targetText;
    private final ManaOrbPanel localPlayerMana, enemyPlayerMana;
    public final Text advantageText;
    private final PlayerStatPanel localPlayerStats, enemyPlayerStats;
    private final MulliganConfirmation mulliganConfirmation;
    public UICard preSelectedCard, selectedCard, draggingCard, playingCard, attackingMinion,
            unleashingMinion;
    ModalSelectionPanel modalSelectionPanel;
    Iterator<List<TargetingScheme<?>>> effectTargetingSchemeIterator; // REAL SCHEMES per effect iterator
    Iterator<TargetingScheme<?>> targetingSchemeIterator; // REAL SCHEMES
    TargetingScheme<?> currentTargetingScheme; // REAL SCHEMES
    List<List<TargetList<?>>> effectCumulativeTargets; // REAL CARDS
    List<TargetList<?>> cumulativeTargets; // REAL CARDS
    TargetList<?> currentTargets; // REAL CARDS
    Set<UICard> mulliganChoices;
    double playingX;
    List<UICard> cards;
    Consumer<Integer> onGameEnd;

    public UIBoard(UI ui, int localteam, DataStream ds, Consumer<Integer> onGameEnd) {
        super(ui, new Vector2f(), new Vector2f(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT), "res/ui/uibox.png");
        this.cards = new ArrayList<>();
        this.b = new VisualBoard(this, localteam);
        this.ds = ds;
        this.eventGroupDescriptionContainer = new EventGroupDescriptionContainer(ui, new Vector2f(-0.5f, 0));
        this.eventGroupDescriptionContainer.relpos = true;
        this.eventGroupDescriptionContainer.alignh = -1;
        this.addChild(this.eventGroupDescriptionContainer);
        this.cardSelectPanel = new CardSelectPanel(ui, this);
        this.cardSelectPanel.setZ(UI_Z_TOP);
        this.addChild(this.cardSelectPanel);
        this.cardSelectPanel.setVisible(false);
        this.endTurnButton = new EndTurnButton(ui, this);
        this.endTurnButton.setVisible(false);
        this.addChild(this.endTurnButton);
        this.targetText = new Text(ui, new Vector2f(), "Target", 400, 24, 30, 0, -1);
        this.targetText.setZ(999);
        this.advantageText = new Text(ui, new Vector2f(-0.25f, -0.4f), "Advantage Text", 400, 24, 30, -1, -1);
        this.advantageText.relpos = true;
        this.advantageText.setZ(1);
        this.addChild(this.targetText);
        this.addChild(this.advantageText);

        this.localPlayerMana = new ManaOrbPanel(ui, new Vector2f(0.2f, 0.5f));
        this.localPlayerMana.alignv = 1;
        this.localPlayerMana.relpos = true;
        this.localPlayerMana.setZ(1);
        this.enemyPlayerMana = new ManaOrbPanel(ui, new Vector2f(-0.2f, -0.5f));
        this.enemyPlayerMana.alignv = -1;
        this.enemyPlayerMana.relpos = true;
        this.enemyPlayerMana.setZ(1);
        this.addChild(this.localPlayerMana);
        this.addChild(this.enemyPlayerMana);

        this.localPlayerStats = new PlayerStatPanel(ui, new Vector2f(0.5f, 0.2f));
        this.localPlayerStats.alignh = 1;
        this.localPlayerStats.relpos = true;
        this.localPlayerStats.setZ(1);
        this.enemyPlayerStats = new PlayerStatPanel(ui, new Vector2f(0.5f, -0.2f));
        this.enemyPlayerStats.alignh = 1;
        this.enemyPlayerStats.relpos = true;
        this.enemyPlayerStats.setZ(1);
        this.addChild(this.localPlayerStats);
        this.addChild(this.enemyPlayerStats);

        this.mulliganConfirmation = new MulliganConfirmation(ui, new Vector2f(0, 0), () -> {
            this.ds.sendPlayerAction(new MulliganAction(this.b.getPlayer(this.b.localteam), this.mulliganChoices.stream()
                    .map(UICard::getCard)
                    .collect(Collectors.toList()))
                    .toString());
            this.mulliganChoices.clear();
        });
        this.mulliganConfirmation.relpos = true;
        this.mulliganConfirmation.setVisible(false);
        this.mulliganConfirmation.setZ(UICard.Z_MULLIGAN - 1);
        this.addChild(this.mulliganConfirmation);

        this.modalSelectionPanel = new ModalSelectionPanel(ui, i -> {
            if (this.currentTargets instanceof ModalTargetList) {
                ModalTargetList mtl = (ModalTargetList) this.currentTargets;
                if (mtl.targeted.remove(i)) {
                    return false;
                } else {
                    mtl.targeted.add(i);
                    ModalTargetingScheme mts = (ModalTargetingScheme) this.currentTargetingScheme;
                    if (mts.isFullyTargeted(mtl)) {
                        this.finishCurrentTargetingScheme();
                    }
                    return true;
                }
            }
            return false;
        });
        this.modalSelectionPanel.setZ(UI_Z_TOP);
        this.addChild(this.modalSelectionPanel);
        this.mulliganChoices = new HashSet<>();
        this.onGameEnd = onGameEnd;
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.b.update(frametime);
        this.readDataStream();

        // handle targeting text
        this.targetText.setVisible(true);
        UICard relevantCard = this.getCurrentTargetingCard();
        if (relevantCard != null && currentTargetingScheme != null) {
            this.targetText.setText(currentTargetingScheme.getDescription());
            this.targetText.setPos(
                    new Vector2f(relevantCard.getPos().x,
                            relevantCard.getPos().y
                                    + (relevantCard.getHeight(false) / 2)),
                    1);
        } else {
            this.targetText.setVisible(false);
        }
        // end handling targeting text

        Player relevantLocalPlayer = this.b.currentPlayerTurn == this.b.localteam ? this.b.realBoard.getPlayer(this.b.localteam) : this.b.getPlayer(this.b.localteam);
        this.localPlayerMana.updateMana(relevantLocalPlayer.mana, relevantLocalPlayer.maxmana);
        this.enemyPlayerMana.updateMana(this.b.getPlayer(this.b.localteam * -1).mana, this.b.getPlayer(this.b.localteam * -1).maxmana);

        this.localPlayerStats.updateStats(relevantLocalPlayer);
        this.enemyPlayerStats.updateStats(this.b.getPlayer(this.b.localteam * -1));

        this.mulliganConfirmation.setEnableInput(!this.b.getPlayer(this.b.localteam).mulliganed);
        this.mulliganConfirmation.setVisible(this.b.mulligan && !this.b.getPlayer(this.b.localteam).getHand().isEmpty());
        // reset some pending stuff
        // move the cards to their respective positions
        for (int team : List.of(-1, 1)) {
            List<BoardObject> bos = team == this.b.localteam ?
                    this.b.pendingPlayPositions.getConsumerStateWithPending().stream()
                        .map(bo -> (BoardObject) bo.visualCard)
                        .collect(Collectors.toList())
                    : this.b.getPlayer(team).getPlayArea();
            ListIterator<BoardObject> bosIter = bos.listIterator();
            while (bosIter.hasNext()) {
                int i = bosIter.nextIndex();
                BoardObject bo = bosIter.next();
                if (bo != null) {
                    UICard uic = bo.uiCard;
                    if (!uic.isBeingAnimated()) {
                        uic.draggable = false;
                        uic.setFlippedOver(false);
                        uic.setVisible(true);
                        if (bo.status.equals(CardStatus.BOARD)) {
                            uic.setPos(this.getBoardPosFor(i, team, bos.size()), 0.99);
                        }
                    }
                }
            }
            this.b.getPlayer(team).getLeader().ifPresent(leader -> {
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
            });
            this.b.getPlayer(team).getUnleashPower().ifPresent(up -> {
                UICard uic = up.uiCard;
                if (!uic.isBeingAnimated()) {
                    uic.draggable = false;
                    uic.setFlippedOver(false);
                    uic.setVisible(true);
                    uic.setPos(new Vector2f((float) UNLEASHPOWER_X,
                                    (float) (team == this.b.localteam ? UNLEASHPOWER_Y_LOCAL : UNLEASHPOWER_Y_ENEMY)),
                            1);
                }
            });
            List<Card> hand = this.b.getPlayer(team).getHand();
            if (team == this.b.localteam && this.b.mulligan) {
                for (int i = 0; i < hand.size(); i++) {
                    Card c = hand.get(i);
                    UICard uic = c.uiCard;
                    if (!uic.isBeingAnimated()) {
                        float x = MULLIGAN_FAN_WIDTH * ((i + 0.5f) / hand.size() - 0.5f);
                        float y = this.mulliganChoices.contains(uic) ? MULLIGAN_TOSS_Y : MULLIGAN_KEEP_Y;
                        uic.setVisible(true);
                        uic.setFlippedOver(false);
                        uic.draggable = !this.b.getPlayer(this.b.localteam).mulliganed;
                        if (uic != this.draggingCard) {
                            uic.setPos(new Vector2f(x, y), 0.999);
                        }
                    }
                }
            } else {
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
        if (this.currentTargets != null) {
            if (this.currentTargets instanceof CardTargetList) {
                for (Card card : ((CardTargetList) this.currentTargets).targeted) {
                    drawCardSelected(g, card.visualCard.uiCard);
                }
            }
        }
        if (!this.b.mulligan && this.draggingCard != null && this.draggingCard.getCard() instanceof BoardObject
                && this.draggingCard.getRelPos().y < CARD_PLAY_Y) {
            int pendingSize = this.b.pendingPlayPositions.getConsumerStateWithPending().size();
            int wouldBePos = XToBoardPos(this.ui.lastmousepos.x / Config.WINDOW_WIDTH - 0.5, this.b.localteam, pendingSize + 1);
            float wouldBeX = (boardPosToX(wouldBePos, this.b.localteam, pendingSize + 1) + 0.5f) * Config.WINDOW_WIDTH;
            g.drawLine(wouldBeX, Config.WINDOW_HEIGHT * 0.55f, wouldBeX, Config.WINDOW_HEIGHT * 0.74f);
        }
        // draw pending play
        List<BoardObject> bos = this.b.pendingPlayPositions.getConsumerStateWithPending();
        ListIterator<BoardObject> bosIter = bos.listIterator();
        while (bosIter.hasNext()) {
            int i = bosIter.nextIndex();
            BoardObject bo = (BoardObject) bosIter.next().visualCard;
            if (bo != null) {
                UICard uic = bo.uiCard;
                if (!bo.status.equals(CardStatus.BOARD)) {
                    // lol
                    Vector2f pos = this.getAbsOfPos(this.getPosOfRel(this.getBoardPosFor(i, this.b.localteam, bos.size())));
                    uic.drawPendingPlayPosition(g, pos);
                }
            }
        }
        // draw pending attack
        for (PendingMinionAttack pma : this.b.pendingMinionAttacks.getPending()) {
            pma.m1.visualCard.uiCard.drawPendingAttack(g, pma.m2.visualCard.uiCard);
        }
        // draw pending unleash
        for (PendingUnleash pu : this.b.pendingUnleashes.getPending()) {
            pu.source.visualCard.uiCard.drawPendingUnleash(g, pu.m.visualCard.uiCard);
        }
        for (VisualBoardAnimation ea : this.b.currentAnimations) {
            if (ea.isStarted()) {
                ea.draw(g);
            }
        }
    }

    private void drawCardSelected(Graphics g, UICard c) {
        g.setColor(Color.red);
        g.drawRect((float) (c.getAbsPos().x - UICard.CARD_DIMENSIONS.x * c.getScale() / 2 * 0.9),
                (float) (c.getAbsPos().y - UICard.CARD_DIMENSIONS.y * c.getScale() / 2 * 0.9),
                (float) (UICard.CARD_DIMENSIONS.x * c.getScale() * 0.9),
                (float) (UICard.CARD_DIMENSIONS.y * c.getScale() * 0.9));
        g.setColor(Color.white);
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
        if (!eg.description.isEmpty()) {
            this.eventGroupDescriptionContainer.addPanel(eg);
        }
    }

    public void onEventGroupPopped(EventGroup eg) {
        if (eg.type.equals(EventGroupType.MINIONATTACKORDER)) {
            for (Card c : eg.cards) {
                c.uiCard.setCombat(false);
            }
        }
        this.eventGroupDescriptionContainer.markDone(eg);
    }

    public void onGameEnd(int team) {
        if (this.onGameEnd != null) {
            this.onGameEnd.accept(team);
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
        this.stopTargeting();
        this.b = new VisualBoard(this, this.b.localteam);
        this.mulliganChoices.clear();
    }

    // auxiliary function for position on board
    private static float boardPosToX(int i, int team, int numCards) {
            return (float) (team * (i - (numCards - 1) / 2.) * BO_SPACING);
    }

    public Vector2f getBoardPosFor(int cardpos, int team, int numCards) {
        return new Vector2f(boardPosToX(cardpos, team, numCards),
                (float) (team == this.b.localteam ? BO_Y_LOCAL : BO_Y_ENEMY));
    }

    private static int XToBoardPos(double x, int team, int numCards) {
        int pos = (int) ((team * x / BO_SPACING) + (numCards / 2.));
        pos = Math.min(Math.max(pos, 0), numCards - 1);
        return pos;
    }

    public void addCard(Card c) {
        UICard uic = new UICard(this.ui, this, c);
        uic.setVisible(false);
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
        if (!this.b.mulligan) {
            this.expandHand = x > 800 && y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
        }
        this.handleTargeting(null);
        this.addParticleSystem(this.getLocalPosOfAbs(new Vector2f(x, y)), UIBoard.PARTICLE_Z_BOARD, DUST_EMISSION_STRATEGY.get());
    }

    public void mousePressedCard(UICard c, int button, int x, int y) {
        this.mouseDownPos.set(x, y);
        this.selectedCard = null;
        this.preSelectedCard = null;
//        this.expandHand = y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
        if (this.b.mulligan) {
            if (c.getCard().status.equals(CardStatus.HAND) && c.getCard().team == this.b.localteam) {
                this.preSelectedCard = c;
                this.draggingCard = c;
            }
        } else if (!this.handleTargeting(c) && !c.isBeingAnimated()) { // if we clicked on a card
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
                    if (this.b.realBoard.getPlayer(this.b.localteam).canUnleash() && !this.b.disableInput) {
                        c.setTargeting(true);
                        this.draggingUnleash = true;
                        this.b.getMinions(this.b.localteam, false, true)
                                .filter(m -> this.b.realBoard.getPlayer(this.b.localteam).canUnleashCard(m.realMinion()))
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
                    this.refreshAnimatedAttackTargets();
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
        if (this.b.mulligan) {
            if (this.draggingCard != null) {
                if (this.draggingCard.getRelPos().y < 0) {
                    this.mulliganChoices.add(this.draggingCard);
                } else {
                    this.mulliganChoices.remove(this.draggingCard);
                }
                this.draggingCard = null;
            }
        } else {
            UICard c = this.cardAtPos(new Vector2f(x, y));
            if (this.attackingMinion != null) { // in middle of ordering attack
                if (c != null && (c.getCard() instanceof Minion) && c.getCard().team != this.b.localteam
                        && this.attackingMinion.getMinion().realMinion().canAttack(c.getMinion().realMinion())) {
                    this.ds.sendPlayerAction(
                            new OrderAttackAction(this.attackingMinion.getMinion().realMinion(), c.getMinion().realMinion())
                                    .toString());
                }
                this.attackingMinion.setOrderingAttack(false);
                this.attackingMinion = null;
                this.refreshAnimatedAttackTargets();
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
                    this.effectTargetingSchemeIterator = this.playingCard.getCard().realCard.getBattlecryTargetingSchemes().iterator();
                    this.effectCumulativeTargets = new ArrayList<>();
                    this.playingX = this.draggingCard.getRelPos().x;
                    this.finishCurrentTargetingScheme();
                }
                this.draggingCard.setDragging(false);
                this.draggingCard = null;
            }
        }
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
        if (this.currentTargetingScheme instanceof CardTargetingScheme) {
            this.animateTargets((CardTargetingScheme) this.currentTargetingScheme, false);
        } else if (this.currentTargetingScheme instanceof ModalTargetingScheme) {
            this.modalSelectionPanel.setVisible(false);
        }
        this.effectTargetingSchemeIterator = null;
        this.targetingSchemeIterator = null;
        this.currentTargetingScheme = null;
        this.currentTargets = null;
        this.effectCumulativeTargets = null;
        this.cumulativeTargets = null;
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

    // make it so either targetingSchemeIterator has a next, or we're done with everything
    public void validateTargetingIterators() {
        if (this.targetingSchemeIterator == null) {
            this.cumulativeTargets = new LinkedList<>();
            this.targetingSchemeIterator = this.effectTargetingSchemeIterator.next().iterator();
        }
        while(!this.targetingSchemeIterator.hasNext()) {
            this.effectCumulativeTargets.add(this.cumulativeTargets);
            this.cumulativeTargets = new LinkedList<>();
            if (this.effectTargetingSchemeIterator.hasNext()) {
                this.targetingSchemeIterator = this.effectTargetingSchemeIterator.next().iterator();
            } else {
                break;
            }
        }
    }
    // attempt to resolve targeting as a player action to send to server
    public void finishCurrentTargetingScheme() {
        while (true) {
            if (this.currentTargets != null) {
                this.cumulativeTargets.add(this.currentTargets);
            }
            if (this.currentTargetingScheme instanceof CardTargetingScheme) {
                this.animateTargets((CardTargetingScheme) this.currentTargetingScheme, false);
            } else if (this.currentTargetingScheme instanceof ModalTargetingScheme) {
                this.modalSelectionPanel.setVisible(false);
            }
            this.validateTargetingIterators();
            if (this.targetingSchemeIterator.hasNext()) {
                this.currentTargetingScheme = this.targetingSchemeIterator.next();
                this.currentTargets = this.currentTargetingScheme.makeList();
                if (this.currentTargetingScheme instanceof CardTargetingScheme) {
                    this.animateTargets((CardTargetingScheme) this.currentTargetingScheme, true);
                }
                if (this.currentTargetingScheme instanceof ModalTargetingScheme) {
                    this.modalSelectionPanel.setVisible(true);
                    this.modalSelectionPanel.setTargetingScheme((ModalTargetingScheme) this.currentTargetingScheme);
                }
                if (this.currentTargetingScheme.isApplicable(this.cumulativeTargets)) {
                    break;
                }
            } else {
                if (this.playingCard != null) {
                    int playPos = 0;
                    if (this.playingCard.getCard() instanceof BoardObject) {
                        int pendingSize = this.b.pendingPlayPositions.getConsumerStateWithPending().size();
                        int pendingPos = XToBoardPos(this.playingX, this.b.localteam, pendingSize + 1);
                        playPos = this.b.pendingPlayPositions.pendingToReal(pendingPos);
                        this.b.pendingPlayPositions.addPendingPositionPreference(pendingPos, (BoardObject) this.playingCard.getCard().realCard);
                    }
                    this.ds.sendPlayerAction(new PlayCardAction(this.b.realBoard.getPlayer(this.b.localteam),
                            this.playingCard.getCard().realCard, playPos,
                            this.effectCumulativeTargets).toString());
                } else if (this.unleashingMinion != null) {
                    // unnecessary check but gets intent across
                    this.ds.sendPlayerAction(new UnleashMinionAction(this.b.realBoard.getPlayer(this.b.localteam),
                            this.unleashingMinion.getMinion().realMinion(), this.effectCumulativeTargets).toString());
                }
                this.stopTargeting();
                break;
            }
        }
    }

    // what happens when u try to click on a card
    public boolean handleTargeting(UICard c) {
        if (this.b.disableInput) {
            return false;
        }
        if (this.currentTargetingScheme instanceof CardTargetingScheme) {
            CardTargetingScheme cts = (CardTargetingScheme) this.currentTargetingScheme;
            CardTargetList ctl = (CardTargetList) this.currentTargets;
            if (c != null && c.getCard().realCard.alive && cts.canTarget(c.getCard().realCard)) {
                if (!this.currentTargets.targeted.remove(c.getCard().realCard)) {
                    ctl.targeted.add(c.getCard().realCard);
                    // whether max targets have been selected or all selectable
                    // targets have been selected
                    if (cts.isFullyTargeted(ctl)) {
                        this.animateTargets(cts, false);
                        this.finishCurrentTargetingScheme();
                    }
                }

                return true;
            } else { // invalid target, so stop targeting
                this.stopTargeting();
            }
        } else if (this.currentTargetingScheme instanceof ModalTargetingScheme) {
            this.stopTargeting();
        }
        return false;
    }

    public void selectUnleashingMinion(UICard c) {
        this.unleashingMinion = c;
        c.setTargeting(true);
        this.effectTargetingSchemeIterator = c.getMinion().realMinion().getUnleashTargetingSchemes().iterator();
        this.effectCumulativeTargets = new ArrayList<>();
        this.finishCurrentTargetingScheme();
    }

    public Stream<UICard> getTargetableCards(CardTargetingScheme t) {
        return this.b.realBoard.getTargetableCards(t)
                .filter(c -> c.visualCard != null)
                .map(c -> c.visualCard.uiCard);
    }

    public void animateTargets(CardTargetingScheme t, boolean activate) {
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

    public void refreshAnimatedTargets() {
        this.refreshAnimatedAttackTargets();
        if (this.currentTargetingScheme instanceof CardTargetingScheme) {
            this.animateTargets((CardTargetingScheme) this.currentTargetingScheme, true);
        }
    }

    private void refreshAnimatedAttackTargets() {
        if (this.attackingMinion == null) {
            this.b.getBoardObjects(this.b.localteam * -1, true, true, false, true)
                    .forEach(target -> target.uiCard.setPotentialTarget(false));
        } else {
            this.b.getBoardObjects(this.b.localteam * -1, true, true, false, true)
                    .filter(target -> target instanceof Minion && this.attackingMinion.getMinion().realMinion().canAttack(((Minion) target).realMinion()))
                    .forEach(target -> target.uiCard.setPotentialTarget(true));
        }
    }

    public ParticleSystem addParticleSystem(Vector2f localPos, int z, EmissionStrategy es) {
        ParticleSystem ps = new ParticleSystem(this.getUI(), localPos, es);
        ps.setZ(z);
        this.addChild(ps);
        return ps;
    }
}
