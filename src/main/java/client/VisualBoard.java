package client;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import client.ui.game.visualboardanimation.VisualBoardAnimation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamage;
import client.ui.game.visualboardanimation.eventgroupanimation.EventGroupAnimation;
import client.ui.game.visualboardanimation.eventgroupanimation.EventGroupAnimationFactory;

import client.ui.game.*;
import client.ui.game.visualboardanimation.eventanimation.*;
import server.*;
import server.ai.*;
import server.card.*;
import server.card.target.CardTargetingScheme;
import server.event.*;
import server.event.eventburst.EventBurst;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import utils.PendingListManager;
import utils.PendingManager;

/**
 * The VisualBoard is a special type of board that the UI (via the UIBoard
 * class) interacts with. It is a representation of board state just like the
 * normal Board, except it animates the events (they don't resolve instantly).
 * It only provides the "model" aspect of the animations, the "view" part is
 * done with the UIBoard.
 */
public class VisualBoard extends Board implements
        PendingPlay.PendingPlayer, PendingPlayPositioner,
        PendingMinionAttack.PendingMinionAttacker, PendingUnleash.PendingUnleasher {
    public static final double MIN_CONCURRENT_EVENT_DELAY = 0.2;

    public final UIBoard uiBoard;
    public final ClientBoard realBoard;
    //shared with realBoard
    public PendingManager<PendingPlay> pendingPlays;
    public PendingListManager<BoardObject> pendingPlayPositions;
    public PendingManager<PendingMinionAttack> pendingMinionAttacks;
    public PendingManager<PendingUnleash> pendingUnleashes;
    final List<String> inputeventliststrings = new LinkedList<>();
    // server sends events in bursts, if not our turn, prevent realboard from advancing past the current animated burst
    final List<EventBurst> bufferedEventBursts = new LinkedList<>();
    public final List<VisualBoardAnimation> currentAnimations = new LinkedList<>();
    public final List<Card> pendingPlayCards = new LinkedList<>();

    // whether this board is not accepting input from the player (i.e., only works when it's the player's turn)
    // Currently this is controlled in EventAnimationTurnStart (enable control) and EndTurnButton (disable control)
    public boolean disableInput = true;

    final EventAnimationFactory eventAnimationFactory;
    final EventGroupAnimationFactory eventGroupAnimationFactory;

    public VisualBoard(UIBoard uiBoard, int localteam) {
        super(localteam);
        this.uiBoard = uiBoard;
        this.pendingPlays = new PendingManager<>() {
            @Override
            public void onProduce(PendingPlay item) {
                if (item.card.visualCard != null && item.card.board.getCurrentPlayerTurn() == item.card.team) {
                    item.card.visualCard.uiCard.addPendingSource(this);
                }
                pendingPlayCards.add(item.card);
            }

            @Override
            public void onConsume(PendingPlay item) {
                if (item.card.visualCard != null) {
                    item.card.visualCard.uiCard.removePendingSource(this);
                }
                pendingPlayCards.remove(item.card);
            }
        };
        this.pendingPlayPositions = new PendingListManager<>();
        this.pendingPlayPositions.trackConsumerState(() -> this.getPlayer(this.getLocalteam()).getPlayArea().stream()
                .map(bo -> (BoardObject) bo.realCard)
                .collect(Collectors.toList()));
        this.pendingMinionAttacks = new PendingManager<>() {
            @Override
            public void onProduce(PendingMinionAttack item) {
                if (item.m1.visualCard != null && item.m1.board.getCurrentPlayerTurn() == item.m1.team) {
                    item.m1.visualCard.uiCard.addPendingSource(this);
                }
            }

            @Override
            public void onConsume(PendingMinionAttack item) {
                if (item.m1.visualCard != null) {
                    item.m1.visualCard.uiCard.removePendingSource(this);
                }
            }
        };
        this.pendingUnleashes = new PendingManager<>() {
            @Override
            public void onProduce(PendingUnleash item) {
                if (item.source.visualCard != null && item.source.board.getCurrentPlayerTurn() == item.source.team) {
                    item.source.visualCard.uiCard.addPendingSource(this);
                }
                if (item.m.visualCard != null && item.m.board.getCurrentPlayerTurn() == item.m.team) {
                    item.m.visualCard.uiCard.addPendingSource(this);
                }
            }

            @Override
            public void onConsume(PendingUnleash item) {
                if (item.source.visualCard != null) {
                    item.source.visualCard.uiCard.removePendingSource(this);
                }
                if (item.m.visualCard != null) {
                    item.m.visualCard.uiCard.removePendingSource(this);
                }
            }
        };
        this.realBoard = new ClientBoard(localteam, this.pendingPlays, this.pendingPlayPositions, this.pendingMinionAttacks, this.pendingUnleashes);
        this.player1.realPlayer = this.realBoard.player1;
        this.player2.realPlayer = this.realBoard.player2;
        this.eventAnimationFactory = new EventAnimationFactory(this);
        this.eventGroupAnimationFactory = new EventGroupAnimationFactory(this);
    }

    public void update(double frametime) {
        this.updateEventAnimation(frametime);
    }

    // wrapper override to also set the advantage text
    @Override
	public <T extends Event> T processEvent(T e) {
        T ret = super.processEvent(e);
		this.uiBoard.advantageText.setText(String.format("Adv: %.4f", AI.evaluateAdvantage(this, this.getLocalteam())));
        this.uiBoard.cardSelectPanel.updateTrackerText();
        this.uiBoard.musicThemeController.updateThemeChoice(this);
        if (e instanceof EventGameEnd) {
            this.uiBoard.onGameEnd(((EventGameEnd) e).victory);
        }
        return ret;
    }

    /**
     * Tries to animate the board based on the bursts provided. Immediately
     * process your bursts, but buffers bursts by the enemy.
     * @param bursts The list of event bursts to process
     */
    @Override
    public void consumeEventBursts(List<EventBurst> bursts) {
        this.bufferedEventBursts.addAll(bursts);
        this.attemptToDequeueBurstStreak();
    }

    // consume a burst, updating the real board and buffering the appropriate animations
    private void consumeBurst(EventBurst eb) {
        if (!eb.eventString.isEmpty()) {
            this.realBoard.parseEventString(eb.eventString);
            this.inputeventliststrings.addAll(List.of(eb.eventString.split(Game.EVENT_END)));
        }
    }

    // take at least one of the buffered bursts and process it
    // process as many consecutive bursts on your turn as you can
    // for the enemy turn, process them one at a time
    // returns true if there is now something in inputeventliststrings
    private boolean dequeueBurst() {
        if (this.bufferedEventBursts.isEmpty()) {
            return false;
        }
        EventBurst eb = this.bufferedEventBursts.remove(0);
        this.consumeBurst(eb);
        if (eb.team == this.getLocalteam()) {
            this.attemptToDequeueBurstStreak();
        }
        return !this.inputeventliststrings.isEmpty();
    }

    // attempt to consume burst streak
    private void attemptToDequeueBurstStreak() {
        while (!this.bufferedEventBursts.isEmpty() && this.bufferedEventBursts.get(0).team == this.getLocalteam()) {
            this.consumeBurst(this.bufferedEventBursts.remove(0));
        }
    }

    public void skipCurrentAnimation() {
        for (Iterator<VisualBoardAnimation> i = this.currentAnimations.iterator(); i.hasNext();) {
            VisualBoardAnimation vba = i.next();
            vba.update(999999); //lmao
            i.remove();
        }
    }

    public void skipAllAnimations() {
        this.skipCurrentAnimation();
        while (!this.inputeventliststrings.isEmpty() || this.dequeueBurst()) {
            String eventOrGroup = this.inputeventliststrings.remove(0);
            if (EventGroup.isPush(eventOrGroup)) {
                EventGroup group = EventGroup.fromString(this, new StringTokenizer(eventOrGroup));
                this.pushEventGroup(group);
                this.uiBoard.onEventGroupPushed(group, false);
            } else if (EventGroup.isPop(eventOrGroup)) {
                EventGroup group = this.popEventGroup();
                this.uiBoard.onEventGroupPopped(group);
            } else {
                Event currentEvent = EventFactory.fromString(this, new StringTokenizer(eventOrGroup));
                if (currentEvent != null && currentEvent.conditions()) {
                    this.processEvent(currentEvent);
                    // EventCreateCard automatically creates some uicards, we need to cleanup
                    if (currentEvent instanceof EventCreateCard) {
                        EventCreateCard evc = (EventCreateCard) currentEvent;
                        for (int i = 0; i < evc.cards.size(); i++) {
                            if (!evc.successful.get(i)) {
                                this.uiBoard.removeUICard(evc.cards.get(i).uiCard);
                            }
                        }
                    }
                }
            }
        }
        // if we skip the EventAnimationTurnStart, input doesn't get re-enabled
        if (this.getCurrentPlayerTurn() == this.getLocalteam()) {
            this.disableInput = false;
        }
    }

    public void updateEventAnimation(double frametime) {
        while ((shouldConcurrentlyAnimate() || this.currentAnimations.isEmpty())
                && (!this.inputeventliststrings.isEmpty() || this.dequeueBurst())) {
            // current set of animations is empty, see what we have to animate
            // next
            VisualBoardAnimation anim = null;
            String eventOrGroup = this.inputeventliststrings.remove(0);
            StringTokenizer st = new StringTokenizer(eventOrGroup);
            if (EventGroup.isPush(eventOrGroup)) {
                EventGroup group = EventGroup.fromString(this, st);
                this.pushEventGroup(group);
                EventGroupAnimation ega = this.eventGroupAnimationFactory.newAnimation(group);
                // Don't animate event groups if they encompass no events
                if (ega == null || this.inputeventliststrings.isEmpty() || !EventGroup.isPop(this.inputeventliststrings.get(0))) {
                    anim = ega;
                }
                this.uiBoard.onEventGroupPushed(group, anim == null || anim.shouldAnimate());
            } else if (EventGroup.isPop(eventOrGroup)) {
                EventGroup group = this.popEventGroup();
                this.uiBoard.onEventGroupPopped(group);
            } else {
                Event currentEvent = EventFactory.fromString(this, st);
                if (currentEvent != null && currentEvent.conditions()) {
                    anim = this.eventAnimationFactory.newAnimation(currentEvent);
                    if (anim == null) {
                        this.processEvent(currentEvent);
                    }
                }
            }
            if (anim != null) {
                // The animation will take care of when to resolve the event
                this.currentAnimations.add(anim);
            }
        }
        double timeUntilLatestProcess = Double.NEGATIVE_INFINITY;
        for (Iterator<VisualBoardAnimation> i = this.currentAnimations.iterator(); i.hasNext();) {
            VisualBoardAnimation vba = i.next();
            if (vba instanceof EventAnimation) {
                // enforce ordering
                EventAnimation<?> ea = (EventAnimation<?>) vba;
                if (ea.getTimeUntilProcess() >= timeUntilLatestProcess) {
                    ea.update(frametime);
                    timeUntilLatestProcess = ea.getTimeUntilProcess() + MIN_CONCURRENT_EVENT_DELAY;
                }
            } else {
                vba.update(frametime);
            }
            if (vba.isFinished()) {
                i.remove();
            }
        }
    }

    private boolean shouldConcurrentlyAnimate() {
        if (this.inputeventliststrings.isEmpty()) {
            return false;
        }
        // try to concurrently animate damage events
        // giga janky but it works, also will not make damage events of different event groups concurrent
        StringTokenizer st = new StringTokenizer(this.inputeventliststrings.get(0));
        return this.peekEventGroup() != null && ((this.peekEventGroup().type.equals(EventGroupType.CONCURRENTDAMAGE)
                && st.nextToken().equals(String.valueOf(EventDamage.ID)) && !this.currentAnimations.isEmpty()
                && this.currentAnimations.get(this.currentAnimations.size() - 1) instanceof EventAnimationDamage)
                || this.peekEventGroup().type.equals(EventGroupType.MINIONCOMBAT));
    }

    @Override
    public Stream<Card> getTargetableCards(CardTargetingScheme t) {
        if (t == null) {
            return Stream.empty();
        }
        return this.getTargetableCards()
                .filter(c -> t.canTarget(c.realCard));
    }

    @Override
    public void setLocalteam(int localteam) {
        super.setLocalteam(localteam);
        this.realBoard.setLocalteam(localteam);
    }

    @Override
    public PendingListManager.Processor<BoardObject> getPendingPlayPositionProcessor() {
        return this.pendingPlayPositions.getConsumer();
    }

    @Override
    public PendingManager.Processor<PendingMinionAttack> getPendingMinionAttackProcessor() {
        return this.pendingMinionAttacks.getConsumer();
    }

    @Override
    public PendingManager.Processor<PendingUnleash> getPendingUnleashProcessor() {
        return this.pendingUnleashes.getConsumer();
    }

    @Override
    public PendingManager.Processor<PendingPlay> getPendingPlayProcessor() {
        return this.pendingPlays.getConsumer();
    }
}
