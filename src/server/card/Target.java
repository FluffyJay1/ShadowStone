package server.card;

import java.util.*;

import client.*;
import client.ui.game.*;
import server.*;
import server.card.effect.*;

/**
 * Target.java is a class that is supposed to handle targeting and their related
 * restrictions. Such applications include player targeting of cards with
 * effects, as well as cards themselves choosing which other cards to affect.
 * 
 * @author Michael
 *
 */
public class Target {
	// ah yes the good ol all in one class
	// targets are handled serverside
	private Effect creator;
	public int maxtargets;
	private List<Card> targets = new ArrayList<Card>();
	public String description;
	boolean ready = false;

	public Target(Effect creator, int numtargets, String description) {
		this.creator = creator;
		this.maxtargets = numtargets;
		this.description = description;
	}

	public Effect getCreator() {
		return this.creator;
	}

	// override this shit with anonymous functions
	public boolean canTarget(Card c) {
		return true;
	}

	// override this shit as well
	public void resolveTargets() {

	}

	public List<Card> getTargets() {
		// TODO: Determine if this is necessary
		if (!this.ready) {
			this.resolveTargets();
		}
		return this.targets;
	}

	public void setTarget(Card target) {
		if (target != null) {
			this.targets.add(target);
		}
		this.ready = true;
	}

	public void setTargets(List<Card> targets) {
		this.targets.addAll(targets);
		this.ready = true;
	}

	// what you're looking at here son is a mistake
	public void setTargetsUI(List<UICard> targets) {
		for (UICard c : targets) {
			this.targets.add(c.getCard());
		}
		this.ready = true;
	}

	public void setRandomTarget() {
		List<Card> cards = new ArrayList<Card>();
		for (Card c : this.creator.owner.board.getCards()) {
			if (this.canTarget(c) && !this.targets.contains(c)) {
				cards.add(c);
			}
		}
		if (cards.size() > 0) {
			this.setTarget(cards.get((int) (Math.random() * cards.size())));
		} else {
			this.setTarget(null);
		}
	}

	public void fillRandomTargets() {
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Card c : this.creator.owner.board.getCards()) {
			if (this.canTarget(c) && !this.targets.contains(c)) {
				cards.add(c);
			}
		}
		for (int i = this.targets.size(); i < this.maxtargets; i++) {
			if (cards.size() > 0) {
				this.setTarget(cards.remove((int) (Math.random() * cards.size())));
			} else {
				this.setTarget(null);
			}
		}
	}

	public boolean ready() { // yeah
		return this.ready;
	}

	public void reset() { // why am i doing this
		this.targets.clear();
		this.ready = false;
	}

	public Target copy() {
		Target ret = new Target(this.creator, this.maxtargets, this.description);
		ret.targets.addAll(this.targets);
		return ret;
	}

	@Override
	public String toString() {
		String ret = (this.creator == null ? "null " : this.creator.toReference()) + this.description + Game.STRING_END
				+ " " + this.maxtargets + " " + this.targets.size() + " ";
		for (Card c : this.targets) {
			ret += (c == null ? "null " : c.toReference());
		}
		return ret;
	}

	public static Target fromString(Board b, StringTokenizer st) {
		Effect creator = Effect.fromReference(b, st);
		String description = st.nextToken(Game.STRING_END).trim();
		st.nextToken(" \n"); // THANKS STRING TOKENIZER
		int maxtargets = Integer.parseInt(st.nextToken());
		int targetsize = Integer.parseInt(st.nextToken());
		Target t = new Target(creator, maxtargets, description);
		for (int i = 0; i < targetsize; i++) {
			Card target = Card.fromReference(b, st);
			t.setTarget(target);
		}
		return t;
	}

	public void copyFromString(Board b, StringTokenizer st) {
		this.targets.clear();
		Effect creator = Effect.fromReference(b, st);
		String description = st.nextToken(Game.STRING_END).trim();
		st.nextToken(" \n"); // THANKS STRING TOKENIZER
		int maxtargets = Integer.parseInt(st.nextToken());
		int targetsize = Integer.parseInt(st.nextToken());
		this.creator = creator;
		this.maxtargets = maxtargets;
		this.description = description;
		for (int i = 0; i < targetsize; i++) {
			Card target = Card.fromReference(b, st);
			this.setTarget(target);
		}
	}

}
