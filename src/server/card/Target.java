package server.card;

import java.util.ArrayList;
import java.util.StringTokenizer;

import client.Game;
import server.Board;
import server.Player;
import server.card.effect.Effect;

public class Target {
	// targets are handled serverside
	private Effect creator;
	public int maxtargets;
	private ArrayList<Card> targets = new ArrayList<Card>();
	public String description;
	boolean ready = false;

	public Target(Effect creator, int numtargets, String description) {
		this.creator = creator;
		this.maxtargets = numtargets;
		this.description = description;
	}

	public Target(Card c) {
		this(null, 1, "");
		this.setTarget(c);
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

	public ArrayList<Card> getTargets() {
		// TODO: Determine if this is necessary
		if (!this.ready) {
			this.resolveTargets();
		}
		return this.targets;
	}

	public void setTarget(Card target) {
		this.targets.add(target);
		this.ready = true;
	}

	public void setTargets(ArrayList<Card> targets) {
		this.targets.addAll(targets);
		this.ready = true;
	}

	public void setRandomTarget() {
		ArrayList<Card> cards = new ArrayList<Card>();
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
		for (int i = this.targets.size(); i < this.maxtargets; i++) {
			this.setRandomTarget();
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

	public String toString() {
		String ret = (this.creator == null ? "null" : this.creator.toReference()) + this.description + Game.STRING_END
				+ this.maxtargets + " " + this.targets.size() + " ";
		for (Card c : this.targets) {
			ret += (c == null ? "null " : c.toReference());
		}
		return ret;
	}

	public static Target fromString(Board b, StringTokenizer st) {
		Effect creator = Effect.fromReference(b, st);
		String description = st.nextToken(Game.STRING_END);
		int maxtargets = Integer.parseInt(st.nextToken());
		int targetsize = Integer.parseInt(st.nextToken());
		Target t = new Target(creator, maxtargets, description);
		for (int i = 0; i < targetsize; i++) {
			Card target = Card.fromReference(b, st);
			t.setTarget(target);
		}
		return t;
	}

}
