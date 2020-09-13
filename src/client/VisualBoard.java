package client;

import java.util.*;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.*;
import client.ui.game.eventanimation.*;
import client.ui.game.eventanimation.attack.*;
import client.ui.game.eventanimation.board.*;
import server.*;
import server.ai.*;
import server.card.*;
import server.event.*;
import utils.*;

public class VisualBoard extends Board implements DefaultMouseListener {
	// distance mouse can move between mouse down and mouse up for it to count
	// as a click
	public static final double CLICK_DISTANCE_THRESHOLD = 5;
	public static final double CARD_SCALE_DEFAULT = 1, CARD_SCALE_HAND = 0.75, CARD_SCALE_HAND_EXPAND = 1.2,
			CARD_SCALE_BOARD = 1, CARD_SCALE_ABILITY = 1.5, CARD_SCALE_TARGET = 1.2, CARD_SCALE_ATTACK = 1.3,
			CARDS_SCALE_PLAY = 2.5;
	// public UI ui;
	public UIBoard uiBoard;
	public Board realBoard;
	Vector2f mouseDownPos = new Vector2f();
	// public Card preSelectedCard, selectedCard, draggingCard, playingCard,
	// visualPlayingCard;
	ArrayList<Card> targetedCards = new ArrayList<Card>();
	int playingX;
	// public Minion attackingMinion, unleashingMinion;
	List<String> inputeventliststrings = new LinkedList<String>();
	public List<EventAnimation> currentAnimations = new LinkedList<EventAnimation>();

	public boolean disableInput = false;
	boolean expandHand = false;
	boolean draggingUnleash = false;

	public VisualBoard(UIBoard uiBoard) {
		this(uiBoard, 1);

		// this.cardSelectPanel.draggable = true;
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
	}

	public void update(double frametime) {
		this.updateEventAnimation(frametime);
	}

	public void draw(Graphics g) {

	}

	@Override
	public List<Event> resolveAll(List<Event> eventlist, boolean loopprotection) {
		// at one point in time only god and i knew why i wrote this, now only
		// god knows
		new Exception("this shouldn't happen lmao").printStackTrace();
		return null;

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
		for (Iterator<EventAnimation> i = this.currentAnimations.iterator(); i.hasNext();) {
			EventAnimation ea = i.next();
			i.remove();
		}
	}

	public void skipAllAnimations() {
		this.skipCurrentAnimation();
		while (!this.inputeventliststrings.isEmpty()) {
			StringTokenizer st = new StringTokenizer(this.inputeventliststrings.remove(0));
			Event currentEvent = Event.createFromString(this, st);
			if (currentEvent != null && currentEvent.conditions()) {
				LinkedList<Event> lmao = new LinkedList<Event>();
				currentEvent.resolve(lmao, false);
				this.uiBoard.advantageText
						.setText(String.format("Adv: %.4f", AI.evaluateAdvantage(this, this.localteam)));
				if (currentEvent instanceof EventTurnStart) {
					this.disableInput = ((EventTurnStart) currentEvent).p.team != this.realBoard.localteam;
				}
			}
		}
	}

	public void updateEventAnimation(double frametime) {
		for (Iterator<EventAnimation> i = this.currentAnimations.iterator(); i.hasNext();) {
			EventAnimation ea = i.next();
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
				LinkedList<Event> lmao = new LinkedList<Event>();
				currentEvent.resolve(lmao, false);
				this.uiBoard.advantageText
						.setText(String.format("Adv: %.4f", AI.evaluateAdvantage(this, this.localteam)));
				// TODO refactor this godforsaken thing and use each card's respective
				// eventaniation for these events
				// e.g. tiny throwing a rock for his attack
				if (currentEvent instanceof EventMinionAttack) {
					EventAnimation anim = new EventAnimationMinionAttack();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventMinionAttackDamage) {
					EventAnimation anim = new EventAnimationMinionAttackDamage();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventDamage) {
					EventAnimation anim = new EventAnimationDamage();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventRestore) {
					EventAnimation anim = new EventAnimationRestore();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventCardDamage) {
					EventAnimation anim = new EventAnimationCardDamage();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventEffectDamage) {
					EventAnimation anim = new EventAnimationEffectDamage();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventUnleash) {
					EventAnimation anim = new EventAnimationUnleash();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventTurnStart) {
					EventAnimation anim = new EventAnimationTurnStart();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
					this.disableInput = ((EventTurnStart) currentEvent).p.team != this.realBoard.localteam;
				} else if (currentEvent instanceof EventPlayCard) {
					EventAnimationBoard anim = new EventAnimationPlayCard();
					anim.init(currentEvent, this.uiBoard);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventPutCard) {
					EventAnimationBoard anim = new EventAnimationPutCard();
					anim.init(currentEvent, this.uiBoard);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventBattlecry) {
					EventBattlecry e = (EventBattlecry) currentEvent;
					if (!(e.effect.owner instanceof Spell)) {
						EventAnimation anim = new EventAnimationBattlecry();
						anim.init(currentEvent);
						this.currentAnimations.add(anim);
					}

				} else if (currentEvent instanceof EventLastWords) {
					EventAnimation anim = new EventAnimationLastWords();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventFlag) {
					EventAnimation anim = new EventAnimationFlag();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventClash) {
					EventAnimation anim = new EventAnimationClash();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventOnAttack) {
					EventAnimation anim = new EventAnimationOnAttack();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventOnAttacked) {
					EventAnimation anim = new EventAnimationOnAttacked();
					anim.init(currentEvent);
					this.currentAnimations.add(anim);
				} else if (currentEvent instanceof EventAddEffect) {
					EventAddEffect e = (EventAddEffect) currentEvent;
					if (!e.c.isEmpty()) {
						EventAnimation anim = new EventAnimationAddEffect();
						anim.init(currentEvent);
						this.currentAnimations.add(anim);
					}
				} else if (currentEvent instanceof EventRemoveEffect) {
					EventRemoveEffect e = (EventRemoveEffect) currentEvent;
					if (e.c != null) {
						EventAnimation anim = new EventAnimationRemoveEffect();
						anim.init(currentEvent);
						this.currentAnimations.add(anim);
					}
				} else if (currentEvent instanceof EventGameEnd) {
					EventAnimationBoard anim = new EventAnimationGameEnd();
					anim.init(currentEvent, this.uiBoard);
					this.currentAnimations.add(anim);
				}
			}
		}
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		// this.ui.mouseMoved(oldx, oldy, newx, newy);
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		// this.ui.mouseDragged(oldx, oldy, newx, newy);
		// if (this.draggingCard != null) {
		// this.draggingCard.targetpos.add(new Vector2f(newx, newy).sub(new
		// Vector2f(oldx, oldy)));
		// }
	}

	@Override
	public void mouseWheelMoved(int change) {
		// this.ui.mouseWheelMoved(change);
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
