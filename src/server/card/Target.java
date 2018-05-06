package server.card;

public class Target {
	public Card target;
	public String description;

	public Target(String description) {
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
