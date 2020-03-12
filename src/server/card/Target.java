package server.card;

import java.util.*;

import client.*;
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
public class Target implements Cloneable {
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

	public void addCard(Card target) {
		if (target != null) {
			this.targets.add(target);
		}
		this.checkReady();
	}

	public void removeCards(Card target) {
		if (target != null) {
			this.ready = !this.targets.remove(target);
		}
		this.checkReady();
	}

	public void setCards(List<Card> targets) {
		this.targets.addAll(targets);
		this.checkReady();
	}

	public void setRandomCards() {
		List<Card> cards = new ArrayList<Card>();
		for (Card c : this.creator.owner.board.getTargetableCards()) {
			if (this.canTarget(c) && !this.targets.contains(c)) {
				cards.add(c);
			}
		}
		if (cards.size() > 0) {
			this.addCard(cards.get((int) (Math.random() * cards.size())));
		} else {
			this.addCard(null);
		}
	}

	public void fillRandomCards() {
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Card c : this.creator.owner.board.getTargetableCards()) {
			if (this.canTarget(c) && !this.targets.contains(c)) {
				cards.add(c);
			}
		}
		for (int i = this.targets.size(); i < this.maxtargets; i++) {
			if (cards.size() > 0) {
				this.addCard(cards.remove((int) (Math.random() * cards.size())));
			} else {
				this.addCard(null);
			}
		}
	}

	// if the maximum number of targets is selected, or all targetable cards
	// have been targeted
	public boolean isFullyTargeted(Board b) {
		return this.getTargets().size() >= this.maxtargets
				|| this.getTargets().size() == b.getTargetableCards(this).size();
	}

	// when the target is deemed ready for the first time, we assume it stays
	// ready until it gets reset, so we don't need to check if it's fully
	// targeted
	private void checkReady() {
		if (!this.ready && this.isFullyTargeted(this.creator.owner.board)) {
			this.ready = true;
		}
	}

	/**
	 * A target is deemed to be ready if no more targets can be selected from
	 * the board.
	 * 
	 * @return whether the target is ready
	 */
	public boolean isReady() { // yeah
		this.checkReady();
		return this.ready;
	}

	public void reset() { // why am i doing this
		this.targets.clear();
		this.ready = false;
	}

	public static void resetList(List<Target> targets) {
		if (targets != null) {
			for (Target t : targets) {
				t.reset();
			}
		}
	}

	public static Target firstUnsetTarget(List<Target> targets) {
		if (targets != null) {
			for (Target t : targets) {
				if (!t.isReady()) {
					return t;
				}
			}
		}
		return null;
	}

	public static void setListFromString(List<Target> targets, Board b, StringTokenizer st) {
		int num = Integer.parseInt(st.nextToken());
		for (int i = 0; i < num; i++) {
			targets.get(i).copyFromString(b, st);
		}
	}

	@Override
	public Target clone() {
		Target ret = null;
		try {
			ret = (Target) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ret.targets = new LinkedList<Card>();
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
			t.addCard(target);
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
			this.addCard(target);
		}
	}

	public static String listToString(List<Target> list) {
		String ret = list.size() + " ";
		for (Target t : list) {
			ret += t.toString();
		}
		return ret;
	}

}
