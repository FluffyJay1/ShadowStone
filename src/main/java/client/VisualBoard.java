package client;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.ui.game.*;
import client.ui.game.eventanimation.*;
import server.*;
import server.ai.*;
import server.card.*;
import server.event.*;
import server.resolver.Resolver;

/**
 * The VisualBoard is a special type of board that the UI (via the UIBoard
 * class) interacts with. It is a representation of board state just like the
 * normal Board, except it animates the events (they don't resolve instantly).
 * It only provides the "model" aspect of the animations, the "view" part is
 * done with the UIBoard.
 */
public class VisualBoard extends Board {
    public UIBoard uiBoard;
    public Board realBoard;
    Vector2f mouseDownPos = new Vector2f();
    ArrayList<Card> targetedCards = new ArrayList<>();
    int playingX;
    List<String> inputeventliststrings = new LinkedList<>();
    public List<EventAnimation<Event>> currentAnimations = new LinkedList<>();

    // whether this board is not accepting input from the player (i.e., only works when it's the player's turn)
    // Currently this is controlled in EventAnimationTurnStart (enable control) and EndTurnButton (disable control)
    public boolean disableInput = false;

    EventAnimationFactory animationFactory;

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
        this.animationFactory = new EventAnimationFactory(this);
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
            String event = st.nextToken();
            this.inputeventliststrings.add(event);
        }

    }

    public void skipCurrentAnimation() {
        for (Iterator<EventAnimation<Event>> i = this.currentAnimations.iterator(); i.hasNext();) {
            EventAnimation<Event> ea = i.next();
            ea.update(999999); //lmao
            i.remove();
        }
    }

    public void skipAllAnimations() {
        this.skipCurrentAnimation();
        while (!this.inputeventliststrings.isEmpty()) {
            StringTokenizer st = new StringTokenizer(this.inputeventliststrings.remove(0));
            Event currentEvent = Event.createFromString(this, st);
            if (currentEvent != null && currentEvent.conditions()) {
                this.processEvent(null, null, currentEvent);
            }
        }
    }

    public void updateEventAnimation(double frametime) {
        for (Iterator<EventAnimation<Event>> i = this.currentAnimations.iterator(); i.hasNext();) {
            EventAnimation<Event> ea = i.next();
            ea.update(frametime);
            if (ea.isFinished()) {
                i.remove();
            }
        }
        if (this.currentAnimations.isEmpty() && !this.inputeventliststrings.isEmpty()) {
            // current set of animations is empty, see what we have to animate
            // next
            StringTokenizer st = new StringTokenizer(this.inputeventliststrings.remove(0));
            Event currentEvent = Event.createFromString(this, st);
            if (currentEvent != null && currentEvent.conditions()) {
                // TODO: handle concurrent animations
                EventAnimation<Event> anim = this.animationFactory.newAnimation(currentEvent);
                // The animation will take care of when to resolve the event
                this.currentAnimations.add(anim);
                // concurrent minion attack animation
                // not the prettiest solution but whatever
                if (currentEvent instanceof EventDamage && ((EventDamage) currentEvent).minionAttack && !this.inputeventliststrings.isEmpty()) {
                    Event nextEvent = Event.createFromString(this, new StringTokenizer(this.inputeventliststrings.remove(0)));
                    if (nextEvent instanceof EventDamage && ((EventDamage)nextEvent).minionAttack) {
                        this.currentAnimations.add(this.animationFactory.newAnimation(nextEvent));
                    }
                }
            }
        }
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
