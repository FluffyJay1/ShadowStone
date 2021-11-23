package client;

import java.util.*;

import client.ui.game.visualboardanimation.VisualBoardAnimation;
import client.ui.game.visualboardanimation.eventgroupanimation.EventGroupAnimationFactory;
import org.newdawn.slick.geom.*;

import client.ui.game.*;
import client.ui.game.visualboardanimation.eventanimation.*;
import server.*;
import server.ai.*;
import server.card.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.Resolver;

/**
 * The VisualBoard is a special type of board that the UI (via the UIBoard
 * class) interacts with. It is a representation of board state just like the
 * normal Board, except it animates the events (they don't resolve instantly).
 * It only provides the "model" aspect of the animations, the "view" part is
 * done with the UIBoard.
 */
public class VisualBoard extends Board {
    public static final double MIN_CONCURRENT_EVENT_DELAY = 0.2;

    public UIBoard uiBoard;
    public Board realBoard;
    Vector2f mouseDownPos = new Vector2f();
    ArrayList<Card> targetedCards = new ArrayList<>();
    int playingX;
    List<String> inputeventliststrings = new LinkedList<>();
    public List<VisualBoardAnimation> currentAnimations = new LinkedList<>();

    // whether this board is not accepting input from the player (i.e., only works when it's the player's turn)
    // Currently this is controlled in EventAnimationTurnStart (enable control) and EndTurnButton (disable control)
    public boolean disableInput = false;

    EventAnimationFactory eventAnimationFactory;
    EventGroupAnimationFactory eventGroupAnimationFactory;
    public VisualBoard(UIBoard uiBoard) {
        this(uiBoard, 1);
    }

    public VisualBoard(UIBoard uiBoard, int localteam) {
        super();
        this.uiBoard = uiBoard;
        this.isClient = true;
        this.isServer = false;
        this.realBoard = new Board();
        this.realBoard.isClient = true;
        this.realBoard.isServer = false;
        this.player1.realPlayer = this.realBoard.player1;
        this.player2.realPlayer = this.realBoard.player2;
        this.realBoard.localteam = localteam;
        this.localteam = localteam;
        this.eventAnimationFactory = new EventAnimationFactory(this);
        this.eventGroupAnimationFactory = new EventGroupAnimationFactory(this);
    }

    public void update(double frametime) {
        this.updateEventAnimation(frametime);
    }

    // wrapper override to also set the advantage text
    @Override
	public <T extends Event> T processEvent(List<Resolver> rl, List<Event> el, T e) {
        T ret = super.processEvent(rl, el, e);
		this.uiBoard.advantageText.setText(String.format("Adv: %.4f", AI.evaluateAdvantage(this, this.localteam)));
        return ret;
    }

    // also updates the underlying real board of the events
    @Override
    public synchronized void parseEventString(String s) {
        this.realBoard.parseEventString(s);
        StringTokenizer st = new StringTokenizer(s, "\n");
        while (st.hasMoreTokens()) {
            String eventOrGroup = st.nextToken();
            this.inputeventliststrings.add(eventOrGroup);
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
        while (!this.inputeventliststrings.isEmpty()) {
            String eventOrGroup = this.inputeventliststrings.remove(0);
            if (!EventGroup.isGroup(eventOrGroup)) {
                Event currentEvent = Event.createFromString(this, new StringTokenizer(eventOrGroup));
                if (currentEvent != null && currentEvent.conditions()) {
                    this.processEvent(null, null, currentEvent);
                }
            }
        }
    }

    public void updateEventAnimation(double frametime) {
        double timeUntilLatestProcess = Double.NEGATIVE_INFINITY;
        for (Iterator<VisualBoardAnimation> i = this.currentAnimations.iterator(); i.hasNext();) {
            VisualBoardAnimation vba = i.next();
            if (vba instanceof EventAnimation) {
                // enforce ordering
                EventAnimation<Event> ea = (EventAnimation<Event>) vba;
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
        while ((shouldConcurrentlyAnimate() || this.currentAnimations.isEmpty())
                && !this.inputeventliststrings.isEmpty()) {
            // current set of animations is empty, see what we have to animate
            // next
            VisualBoardAnimation anim = null;
            String eventOrGroup = this.inputeventliststrings.remove(0);
            StringTokenizer st = new StringTokenizer(eventOrGroup);
            if (EventGroup.isPush(eventOrGroup)) {
                EventGroup group = EventGroup.fromString(this, st);
                this.pushEventGroup(group);
                anim = this.eventGroupAnimationFactory.newAnimation(group);
            } else if (EventGroup.isPop(eventOrGroup)) {
                EventGroup group = this.popEventGroup();
                // TODO do something special
            } else {
                Event currentEvent = Event.createFromString(this, st);
                if (currentEvent != null && currentEvent.conditions()) {
                    anim = this.eventAnimationFactory.newAnimation(currentEvent);
                    if (anim == null) {
                        this.processEvent(null, null, currentEvent);
                    }
                }
            }
            if (anim != null) {
                // The animation will take care of when to resolve the event
                this.currentAnimations.add(anim);
            }
        }
    }

    private boolean shouldConcurrentlyAnimate() {
        return this.peekEventGroup() != null && this.peekEventGroup().type.equals(EventGroupType.MINIONCOMBAT);
    }

    @Override
    public LinkedList<Card> getTargetableCards(Target t) {
        LinkedList<Card> list = new LinkedList<Card>();
        if (t == null) {
            return list;
        }
        for (Card c : this.getTargetableCards()) {
            if (t.canTarget(c.realCard)) {
                list.add(c);
            }
        }
        return list;
    }

}
