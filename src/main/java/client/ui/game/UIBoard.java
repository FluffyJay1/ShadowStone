package client.ui.game;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import client.Game;
import client.ui.Animation;
import client.ui.game.visualboardanimation.VisualBoardAnimation;
import client.ui.game.visualboardanimation.eventanimation.board.EventAnimationPlayCard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.*;
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
import server.event.eventburst.EventBurst;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.playeraction.*;

public class UIBoard extends UIBox {
    public static final double CLICK_DISTANCE_THRESHOLD = 5;
    public static final float BO_SPACING = 0.1f, BO_Y_LOCAL = 0.105f, BO_Y_ENEMY = -0.115f;
    public static final double LEADER_Y_LOCAL = 0.38, LEADER_Y_ENEMY = -0.4;
    public static final double UNLEASHPOWER_X = 0.07, UNLEASHPOWER_Y_LOCAL = 0.27, UNLEASHPOWER_Y_ENEMY = -0.29;
    public static final double HAND_X_LOCAL = 0.19, HAND_X_ENEMY = 0.28, HAND_X_EXPAND_LOCAL = 0.175;
    public static final double HAND_Y_LOCAL = 0.38, HAND_Y_ENEMY = -0.41, HAND_Y_EXPAND_LOCAL = 0.33;
    public static final double HAND_X_SCALE_LOCAL = 0.22, HAND_X_SCALE_ENEMY = 0.26, HAND_X_SCALE_EXPAND_LOCAL = 0.40;
    private static final float MULLIGAN_FAN_WIDTH = 0.6f, MULLIGAN_KEEP_Y = 0.2f, MULLIGAN_TOSS_Y = -0.2f;
    public static final double CARD_PLAY_Y = 0.2, HAND_EXPAND_Y = 0.30;
    private static final float EMOTE_Y_LOCAL = 0.25f, EMOTE_Y_ENEMY = -0.27f, EMOTE_SELECT_Y = 0.34f;
    public static final double DECK_X = 0.35, DECK_Y_LOCAL = 0.2, DECK_Y_ENEMY = -0.2;
    public static final float PENDING_X = -0.35f, PENDING_Y = -0.25f, PENDING_Y_SPACING = 0.3f;
    public static final int PARTICLE_Z_BOARD = 1, PARTICLE_Z_SPECIAL = 5, UI_Z_TOP = 10;
    public static final Vector2f TARGETING_CARD_POS = new Vector2f(-0.4f, -0.22f);
    private static final double ELUSIVE_TIME_PER_CYCLE = 2.5;
    private static final double TEAM_ASSIGN_TIME = 4;

    private static final Interpolation<Double> ELUSIVE_ALPHA_INTERPOLATION = new ComposedInterpolation<>(new ClampedInterpolation(0, ELUSIVE_TIME_PER_CYCLE),
            new ComposedInterpolation<>(
                    new SequentialInterpolation<>(
                            List.of(new SpringInterpolation(1), new ComposedInterpolation<>(new SpringInterpolation(1), new LinearInterpolation(1, 0))),
                            List.of(0.5, 0.5)
                    ),
                    new LinearInterpolation(0.1, 0.5)
            ));

    private static final Interpolation<Double> TEAM_ASSIGN_ALPHA_INTERPOLATION = new ComposedInterpolation<>(new ClampedInterpolation(0, TEAM_ASSIGN_TIME),
            new SequentialInterpolation<>(
                    List.of(new LinearInterpolation(0, 1), new ConstantInterpolation(1), new LinearInterpolation(1, 0)),
                    List.of(0.1, 0.7, 0.2)
            ));

    private static final Supplier<EmissionStrategy> DUST_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(6),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/misc/dust.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.4)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.1, new Vector2f(0, 700),
                            () -> new LinearInterpolation(0.4, 0),
                            () -> new QuadraticInterpolationA(1, 0, -4)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 350)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
        );

    public VisualBoard b;
    public final DataStream ds;
    private final Thread dsReadingThread;
    private final SynchronousQueue<Runnable> bufferedUpdates; // stuff that the dsReadingThread wants to do on this thread
    boolean expandHand = false;
    boolean draggingUnleash = false;
    boolean skipNextEventAnimations = false;
    private double elusiveTimer, teamAssignTimer;
    Vector2f mouseDownPos = new Vector2f();
    final EventGroupDescriptionContainer eventGroupDescriptionContainer;
    public final CardSelectPanel cardSelectPanel;
    final EndTurnButton endTurnButton;
    public final Text targetText;
    private final ManaOrbPanel localPlayerMana, enemyPlayerMana;
    public final Text advantageText;
    private final PlayerStatPanel localPlayerStats, enemyPlayerStats;
    public final ClassCraftTrackerPanel localPlayerTracker, enemyPlayerTracker;
    private final MulliganConfirmation mulliganConfirmation;
    private final EmoteSelectPanel emoteSelectPanel;
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
    public boolean connectionClosed;
    public Runnable onConnectionClosed;

    public MusicThemeController musicThemeController;

    public UIBoard(UI ui, DataStream ds, Consumer<Integer> onGameEnd, Runnable onConnectionClosed) {
        super(ui, new Vector2f(), new Vector2f(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT), "ui/uibox.png");
        this.cards = new ArrayList<>();
        this.b = new VisualBoard(this, 0);
        this.ds = ds;
        this.bufferedUpdates = new SynchronousQueue<>();

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

        this.localPlayerTracker = new ClassCraftTrackerPanel(ui, new Vector2f(0.5f, 0.3f));
        this.localPlayerTracker.alignh = 1;
        this.localPlayerTracker.relpos = true;
        this.localPlayerTracker.setZ(1);
        this.enemyPlayerTracker = new ClassCraftTrackerPanel(ui, new Vector2f(0.5f, -0.3f));
        this.enemyPlayerTracker.alignh = 1;
        this.enemyPlayerTracker.relpos = true;
        this.enemyPlayerTracker.setZ(1);
        this.addChild(this.localPlayerTracker);
        this.addChild(this.enemyPlayerTracker);

        this.mulliganConfirmation = new MulliganConfirmation(ui, new Vector2f(0, 0), () -> {
            try {
                this.ds.sendPlayerAction(new MulliganAction(this.b.getPlayer(this.b.getLocalteam()), this.mulliganChoices.stream()
                        .map(UICard::getCard)
                        .collect(Collectors.toList()))
                        .toString());
                this.mulliganChoices.clear();
            } catch (IOException e) {
                this.close();
            }
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

        this.emoteSelectPanel = new EmoteSelectPanel(ui, new Vector2f(0, EMOTE_SELECT_Y), this, emote -> {
            this.showEmote(this.b.getLocalteam(), emote);
            try {
                this.ds.sendEmote(emote);
            } catch (IOException e) {
                this.close();
            }
        });
        this.emoteSelectPanel.relpos = true;
        this.emoteSelectPanel.setVisible(false);
        this.emoteSelectPanel.setZ(UI_Z_TOP);
        this.addChild(this.emoteSelectPanel);

        this.connectionClosed = false;
        this.onGameEnd = onGameEnd;
        this.onConnectionClosed = onConnectionClosed; // im sorry elizabeth

        this.teamAssignTimer = Double.POSITIVE_INFINITY;

        this.musicThemeController = new MusicThemeController();

        this.dsReadingThread = new Thread(() -> {
            while (!this.connectionClosed) {
                this.readDataStream();
            }
        });
        this.dsReadingThread.start();
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.b.update(frametime);
        for (Runnable update = bufferedUpdates.poll(); update != null; update = bufferedUpdates.poll()) {
            update.run();
        }

        this.elusiveTimer = (this.elusiveTimer + frametime) % ELUSIVE_TIME_PER_CYCLE;
        if (this.teamAssignTimer < TEAM_ASSIGN_TIME) {
            this.teamAssignTimer += frametime;
        }
        // handle targeting text
        this.targetText.setVisible(true);
        UICard relevantCard = this.getCurrentTargetingCard();
        if (relevantCard != null && this.currentTargetingScheme != null) {
            this.targetText.setText(this.currentTargetingScheme.getDescription());
            this.targetText.setPos(
                    new Vector2f(relevantCard.getPos().x,
                            relevantCard.getPos().y
                                    + (relevantCard.getHeight(false) / 2)),
                    1);
        } else {
            this.targetText.setVisible(false);
        }
        // end handling targeting text

        Player realLocalPlayer = this.b.realBoard.getPlayer(this.b.getLocalteam());
        Player realEnemyPlayer = this.b.realBoard.getPlayer(this.b.getLocalteam() * -1);
        this.localPlayerMana.updateMana(realLocalPlayer.mana, realLocalPlayer.maxmana);
        this.enemyPlayerMana.updateMana(realEnemyPlayer.mana, realEnemyPlayer.maxmana);

        this.localPlayerStats.updateStats(realLocalPlayer);
        this.enemyPlayerStats.updateStats(realEnemyPlayer);

        this.localPlayerTracker.updateTrackerText(realLocalPlayer);
        this.enemyPlayerTracker.updateTrackerText(realEnemyPlayer);

        this.mulliganConfirmation.setEnableInput(!this.b.getPlayer(this.b.getLocalteam()).mulliganed);
        this.mulliganConfirmation.setVisible(this.b.mulligan && !this.b.getPlayer(this.b.getLocalteam()).getHand().isEmpty());
        // reset some pending stuff
        // move the cards to their respective positions
        for (int team : List.of(-1, 1)) {
            List<BoardObject> bos = team == this.b.getLocalteam() ?
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
                    uic.setVisible(true);
                    uic.setPos(
                            // oh my this formatting
                            new Vector2f(0,
                                    (float) (team == this.b.getLocalteam() ? LEADER_Y_LOCAL : LEADER_Y_ENEMY)),
                            1);
                }
            });
            this.b.getPlayer(team).getUnleashPower().ifPresent(up -> {
                UICard uic = up.uiCard;
                if (!uic.isBeingAnimated()) {
                    uic.draggable = false;
                    uic.setVisible(true);
                    uic.setPos(new Vector2f((float) UNLEASHPOWER_X,
                                    (float) (team == this.b.getLocalteam() ? UNLEASHPOWER_Y_LOCAL : UNLEASHPOWER_Y_ENEMY)),
                            1);
                }
            });
            List<Card> hand = this.b.getPlayer(team).getHand();
            if (team == this.b.getLocalteam() && this.b.mulligan) {
                for (int i = 0; i < hand.size(); i++) {
                    Card c = hand.get(i);
                    UICard uic = c.uiCard;
                    if (!uic.isBeingAnimated()) {
                        float x = MULLIGAN_FAN_WIDTH * ((i + 0.5f) / hand.size() - 0.5f);
                        float y = this.mulliganChoices.contains(uic) ? MULLIGAN_TOSS_Y : MULLIGAN_KEEP_Y;
                        uic.setVisible(true);
                        uic.draggable = !this.b.getPlayer(this.b.getLocalteam()).mulliganed;
                        if (uic != this.draggingCard) {
                            uic.setPos(new Vector2f(x, y), 0.999);
                        }
                    }
                }
            } else {
                int idx = 0;
                int handSize = this.b.getPlayer(team).getHand().size();
                if (team == this.b.getLocalteam()) {
                    handSize -= this.b.pendingPlayCards.size();
                    if (this.playingCard != null) {
                        handSize--;
                    }
                    // maximum jank
                    if (!this.b.currentAnimations.isEmpty()) {
                        VisualBoardAnimation anim = this.b.currentAnimations.get(0);
                        if (anim instanceof EventAnimationPlayCard && ((EventAnimationPlayCard) anim).event.c.team == team) {
                            handSize--;
                        }
                    }
                }
                for (Card c : hand) {
                    UICard uic = c.uiCard;
                    // pending cards are positioned separately
                    if (!uic.isBeingAnimated() && !uic.isPending()) {
                        if (team == this.b.getLocalteam() && !this.b.disableInput) {
                            uic.draggable = true;
                        }
                        uic.setVisible(true);
                        // ignore cards that are cards being animated in special ways
                        if (uic != this.playingCard && uic != this.draggingCard) {
                            // ignore the following behemoth of a statement
                            // dont bother understanding it lol
                            // if there's a bug then god have mercy
                            uic.setPos(
                                    new Vector2f(
                                            (float) ((idx - handSize / 2.)
                                                    * (team == this.b.getLocalteam() ? (this.expandHand
                                                    ? (HAND_X_SCALE_EXPAND_LOCAL + handSize * 0.02)
                                                    : HAND_X_SCALE_LOCAL) : HAND_X_SCALE_ENEMY) / handSize
                                                    + (team == this.b.getLocalteam() ? (this.expandHand
                                                    ? (HAND_X_EXPAND_LOCAL - handSize * 0.01)
                                                    : HAND_X_LOCAL) : HAND_X_ENEMY)),
                                            (float) (team == this.b.getLocalteam()
                                                    ? (this.expandHand ? HAND_Y_EXPAND_LOCAL : HAND_Y_LOCAL)
                                                    : HAND_Y_ENEMY)),
                                    0.99);
                        }
                        idx++;
                    }
                }
            }
            // position pending cards
            float pendingY = PENDING_Y;
            for (Card c : this.b.pendingPlayCards) {
                if (c.visualCard != null) {
                    UICard uic = c.visualCard.uiCard;
                    uic.draggable = false;
                    uic.setVisible(true);
                    uic.setPos(new Vector2f(PENDING_X, pendingY), 0.99);
                    pendingY += PENDING_Y_SPACING;
                }
            }
            List<Card> deck = this.b.getPlayer(team).getDeck();
            for (Card c : deck) {
                UICard uic = c.uiCard;
                if (!uic.isBeingAnimated()) {
                    uic.draggable = false;
                    uic.setVisible(true);
                    uic.setPos(new Vector2f((float) DECK_X, (float) (team == this.b.getLocalteam() ? DECK_Y_LOCAL : DECK_Y_ENEMY)), 0.99);
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

        this.musicThemeController.update(frametime);
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
            int wouldBePos = XToBoardPos(this.ui.lastmousepos.x / Config.WINDOW_WIDTH - 0.5, this.b.getLocalteam(), pendingSize + 1);
            float wouldBeX = (boardPosToX(wouldBePos, this.b.getLocalteam(), pendingSize + 1) + 0.5f) * Config.WINDOW_WIDTH;
            g.drawLine(wouldBeX, Config.WINDOW_HEIGHT * 0.55f, wouldBeX, Config.WINDOW_HEIGHT * 0.74f);
        }
        // draw pending play
        List<BoardObject> bos = this.b.pendingPlayPositions.getConsumerStateWithPending();
        ListIterator<BoardObject> bosIter = bos.listIterator();
        while (bosIter.hasNext()) {
            int i = bosIter.nextIndex();
            BoardObject bo = (BoardObject) bosIter.next().visualCard;
            if (bo != null && bo.team == this.b.getCurrentPlayerTurn()) {
                UICard uic = bo.uiCard;
                if (!bo.status.equals(CardStatus.BOARD)) {
                    // lol
                    Vector2f pos = this.getAbsOfPos(this.getPosOfRel(this.getBoardPosFor(i, this.b.getLocalteam(), bos.size())));
                    uic.drawPendingPlayPosition(g, pos);
                }
            }
        }
        // draw pending attack
        for (PendingMinionAttack pma : this.b.pendingMinionAttacks.getPending()) {
            if (this.b.getCurrentPlayerTurn() == pma.m1.team) {
                pma.m1.visualCard.uiCard.drawPendingAttack(g, pma.m2.visualCard.uiCard);
            }
        }
        // draw pending unleash
        for (PendingUnleash pu : this.b.pendingUnleashes.getPending()) {
            if (this.b.getCurrentPlayerTurn() == pu.source.team) {
                pu.source.visualCard.uiCard.drawPendingUnleash(g, pu.m.visualCard.uiCard);
            }
        }
        for (VisualBoardAnimation ea : this.b.currentAnimations) {
            if (ea.isStarted()) {
                ea.draw(g);
            }
        }

        if (this.teamAssignTimer < TEAM_ASSIGN_TIME) {
            UnicodeFont font = Game.getFont(80, true, false);
            g.setFont(font);
            g.setColor(new Color(1f, 1f, 1f, TEAM_ASSIGN_ALPHA_INTERPOLATION.get(this.teamAssignTimer).floatValue()));
            String s = this.b.getLocalteam() == 1 ? "You go first" : "You go second";
            g.drawString(s, Config.WINDOW_WIDTH / 2 - font.getWidth(s) / 2, Config.WINDOW_HEIGHT / 2 - font.getHeight(s) / 2);
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

    // blocking, so this is called from the dsReadingThread
    private void readDataStream() {
        try {
            MessageType mtype = this.ds.receive();
            switch (mtype) {
                case EVENT -> {
                    List<EventBurst> eventBursts = this.ds.readEventBursts();
                    this.bufferedUpdates.put(() -> {
                        this.b.consumeEventBursts(eventBursts);
                        if (this.skipNextEventAnimations) {
                            this.b.skipAllAnimations();
                            this.skipNextEventAnimations = false;
                        }
                    });
                }
                case COMMAND -> {
                    String command = this.ds.readCommand();
                    if (command.equals("reset")) {
                        this.bufferedUpdates.put(this::resetBoard);
                        this.skipNextEventAnimations = true;
                    }
                }
                case EMOTE -> {
                    Emote emote = this.ds.readEmote();
                    if (emote != null) {
                        this.bufferedUpdates.put(() -> this.showEmote(this.b.getLocalteam() * -1, emote));
                    }
                }
                case TEAMASSIGN -> {
                    int team = this.ds.readTeamAssign();
                    this.b.setLocalteam(team);
                    this.teamAssignTimer = 0;
                }
                default -> {
                    this.ds.discardMessage();
                }
            }
        } catch (IOException e) {
            this.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onEventGroupPushed(EventGroup eg, boolean shouldAnimate) {
        if (eg.type.equals(EventGroupType.MINIONATTACKORDER)) {
            for (Card c : eg.cards) {
                c.uiCard.setCombat(true);
            }
        }
        if (!eg.description.isEmpty() && shouldAnimate) {
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
        this.b.skipAllAnimations();
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
        this.b = new VisualBoard(this, this.b.getLocalteam());
        this.mulliganChoices.clear();
    }

    // auxiliary function for position on board
    private float boardPosToX(int i, int team, int numCards) {
            return (float) (team * this.b.getLocalteam() * (i - (numCards - 1) / 2.) * BO_SPACING);
    }

    public Vector2f getBoardPosFor(int cardpos, int team, int numCards) {
        return new Vector2f(boardPosToX(cardpos, team, numCards),
                (float) this.getBoardPosY(team));
    }

    public float getBoardPosY(int team) {
        return team == this.b.getLocalteam() ? BO_Y_LOCAL : BO_Y_ENEMY;
    }

    private int XToBoardPos(double x, int team, int numCards) {
        int pos = (int) ((team * this.b.getLocalteam() * x / BO_SPACING) + (numCards / 2.));
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

    public UICard cardAtPos(float x, float y) {
        for (UICard c : this.cards) {
            if (c.isVisible() && c.pointIsInHitbox(x, y)) {
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
        this.emoteSelectPanel.setVisible(false);
    }

    public void mousePressedCard(UICard c, int button, int x, int y) {
        this.mouseDownPos.set(x, y);
        this.selectedCard = null;
        this.preSelectedCard = null;
//        this.expandHand = y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
        if (this.b.mulligan) {
            if (c.getCard().status.equals(CardStatus.HAND) && c.getCard().team == this.b.getLocalteam()) {
                this.preSelectedCard = c;
                this.draggingCard = c;
            }
        } else if (this.getCurrentTargetingCard() == null && !c.isBeingAnimated()) { // if we clicked on a card
            this.preSelectedCard = c;
            switch (c.getCard().status) {
            case HAND:
                if (c.getCard().team == this.b.getLocalteam()) {
                    if (this.b.realBoard.getPlayer(this.b.getLocalteam()).canPlayCard(c.getCard().realCard)
                            && !this.b.disableInput) {
                        this.draggingCard = c;
                        c.setDragging(true);
                    }
                    this.expandHand = true;
                }
                this.emoteSelectPanel.setVisible(false);
                break;
            case UNLEASHPOWER:
                if (c.getCard().team == this.b.getLocalteam()) {
                    if (this.b.realBoard.getPlayer(this.b.getLocalteam()).canUnleash() && !this.b.disableInput) {
                        c.setTargeting(true);
                        this.draggingUnleash = true;
                        this.refreshAnimatedUnleashTargets();
                    }
                }
                this.emoteSelectPanel.setVisible(false);
                break;
            case LEADER: // TODO allow leader to attac properly
                if (button == 1 && c.getCard().team == this.b.getLocalteam()) { // if right click on friendly leader
                    this.emoteSelectPanel.setVisible(!this.emoteSelectPanel.isVisible());
                    break;
                }
            case BOARD:
                BoardObject bo = (BoardObject) c.getCard();
                if (bo instanceof Minion && bo.realCard.team == this.b.getLocalteam()
                        && ((Minion) bo.realCard).canAttack() && !this.b.disableInput) {
                    this.attackingMinion = c;
                    this.refreshAnimatedAttackTargets();
                    c.setOrderingAttack(true);
                }
                this.emoteSelectPanel.setVisible(false);
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
            UICard c = this.cardAtPos(x, y);
            if (this.attackingMinion != null) { // in middle of ordering attack
                if (c != null && (c.getCard() instanceof Minion) && c.getCard().team != this.b.getLocalteam()
                        && this.attackingMinion.getMinion().realMinion().canAttack(c.getMinion().realMinion())) {
                    try {
                        this.ds.sendPlayerAction(
                                new OrderAttackAction(this.attackingMinion.getMinion().realMinion(), c.getMinion().realMinion())
                                        .toString());
                    } catch (IOException e) {
                        this.close();
                    }
                }
                this.attackingMinion.setOrderingAttack(false);
                this.attackingMinion = null;
                this.refreshAnimatedAttackTargets();
            } else if (this.draggingUnleash) { // in middle of unleashing
                // preselected card is unleashpower
                this.preSelectedCard.setTargeting(false);
                this.draggingUnleash = false;
                this.refreshAnimatedUnleashTargets();
                if (c != null && c.getCard() instanceof Minion && c.getCard().team == this.b.getLocalteam()
                        && this.b.getPlayer(this.b.getLocalteam()).realPlayer.canUnleashCard(c.getCard().realCard)) {
                    this.selectUnleashingMinion(c);
                }
            } else if (this.draggingCard != null) { // in middle of playing card
                if (this.draggingCard.getRelPos().y < CARD_PLAY_Y
                        && this.b.realBoard.getPlayer(this.b.getLocalteam()).canPlayCard(this.draggingCard.getCard().realCard)) {
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

    public void mouseReleasedCard(UICard c, int button, int x, int y) {
        if (!this.handleTargeting(c)) {
            this.mouseReleased(button, x, y);
        }
    }

    // TODO: remove save scumming debug
    @Override
    public void keyPressed(int key, char c) {
        try {
            if (c == 'z') {
                this.ds.sendCommand("save");
            }
            if (c == 'x') {
                this.ds.sendCommand("load");
                this.advantageText.setText("KIRA QUEEN DAISAN NO BAKUDAN");
            }
        } catch (IOException e) {
            this.close();
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
            if (!this.effectTargetingSchemeIterator.hasNext()) {
                return;
            }
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
            if (this.targetingSchemeIterator != null && this.targetingSchemeIterator.hasNext()) {
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
                        int pendingPos = XToBoardPos(this.playingX, this.b.getLocalteam(), pendingSize + 1);
                        playPos = this.b.pendingPlayPositions.pendingToReal(pendingPos);
                        this.b.pendingPlayPositions.addPendingPositionPreference(pendingPos, (BoardObject) this.playingCard.getCard().realCard);
                    }
                    try {
                        this.ds.sendPlayerAction(new PlayCardAction(this.b.realBoard.getPlayer(this.b.getLocalteam()),
                                this.playingCard.getCard().realCard, playPos,
                                this.effectCumulativeTargets).toString());
                    } catch (IOException e) {
                        this.close();
                    }
                } else if (this.unleashingMinion != null) {
                    // unnecessary check but gets intent across
                    try {
                        this.ds.sendPlayerAction(new UnleashMinionAction(this.b.realBoard.getPlayer(this.b.getLocalteam()),
                                this.unleashingMinion.getMinion().realMinion(), this.effectCumulativeTargets).toString());
                    } catch (IOException e) {
                        this.close();
                    }
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
        this.refreshAnimatedUnleashTargets();
        if (this.currentTargetingScheme instanceof CardTargetingScheme) {
            this.animateTargets((CardTargetingScheme) this.currentTargetingScheme, true);
        }
    }

    private void refreshAnimatedAttackTargets() {
        if (this.attackingMinion == null) {
            this.b.getBoardObjects(this.b.getLocalteam() * -1, true, true, false, true)
                    .forEach(target -> target.uiCard.setPotentialTarget(false));
        } else {
            this.b.getBoardObjects(this.b.getLocalteam() * -1, true, true, false, true)
                    .filter(target -> target instanceof Minion && this.attackingMinion.getMinion().realMinion().canAttack(((Minion) target).realMinion()))
                    .forEach(target -> target.uiCard.setPotentialTarget(true));
        }
    }

    private void refreshAnimatedUnleashTargets() {
        if (this.draggingUnleash) {
            this.b.getMinions(this.b.getLocalteam(), false, true)
                    .filter(m -> this.b.realBoard.getPlayer(this.b.getLocalteam()).canUnleashCard(m.realMinion()))
                    .forEach(m -> m.uiCard.setPotentialTarget(true));
        } else {
            this.b.getMinions(this.b.getLocalteam(), false, true)
                    .forEach(m -> m.uiCard.setPotentialTarget(false));
        }
    }

    public ParticleSystem addParticleSystem(Vector2f localPos, int z, EmissionStrategy es) {
        ParticleSystem ps = new ParticleSystem(this.getUI(), localPos, es);
        ps.setZ(z);
        this.addChild(ps);
        return ps;
    }

    public float getElusiveAlpha() {
        return ELUSIVE_ALPHA_INTERPOLATION.get(this.elusiveTimer).floatValue();
    }

    private void showEmote(int team, Emote emote) {
        this.b.getPlayer(team).getLeader().ifPresent(l -> {
            EmoteDisplayUnit edp = new EmoteDisplayUnit(this.ui, new Vector2f(0, team == this.b.getLocalteam() ? EMOTE_Y_LOCAL : EMOTE_Y_ENEMY),
                    l.getTooltip().emoteSet.getLine(emote));
            edp.relpos = true;
            this.addChild(edp);
        });
    }

    /**
     * Perform cleanup when we exit the game, to e.g. go to another screen
     */
    public void exit() {
        this.musicThemeController.stop();
        this.close();
    }

    /**
     * Close datastream connections
     */
    public void close() {
        if (!connectionClosed) {
            this.ds.close();
            this.connectionClosed = true;
            this.onConnectionClosed.run();
            this.dsReadingThread.interrupt();
        }
    }
}
