package server.card;

import java.util.StringTokenizer;

import client.Game;
import server.Board;

public class Target {
	private Card creator, target;
	public String description;
	boolean ready = false;

	public Target(Card creator, String description) {
		this.creator = creator;
		target = null;
		this.description = description;
	}

	// override this shit with anonymous functions
	public Card getCreator() {
		return this.creator;
	}

	public boolean canTarget(Card c) {
		return true;
	}

	public Card getTarget() {
		return this.target;
	}

	public void setTarget(Card target) {
		this.target = target;
		this.ready = true;
	}

	public boolean ready() { // yeah
		return this.ready;
	}

	public void reset() { // why am i doing this
		this.target = null;
		this.ready = false;
	}

	public String toString() {
		return "target " + this.creator.toReference() + this.description + Game.STRING_END
				+ (this.target == null ? "null" : this.target.toReference() + " ");
	}

	public static Target fromString(Board b, StringTokenizer st) {
		Card creator = Card.fromReference(b, st);
		String description = st.nextToken(Game.STRING_END);
		Card target = Card.fromReference(b, st);
		Target t = new Target(creator, description);
		t.setTarget(target);
		return t;
	}

}
