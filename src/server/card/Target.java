package server.card;

public class Target {
	public Card creator, target;
	public String description;

	public Target(Card creator, String description) {
		this.creator = creator;
		target = null;
		this.description = description;
	}

	// override this shit with anonymous functions
	public boolean canTarget(Card c) {
		return true;
	}

	public boolean ready() { // yeah
		return this.target != null;
	}

	public void reset() { // why am i doing this
		this.target = null;
	}

}
