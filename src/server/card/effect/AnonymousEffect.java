package server.card.effect;

import java.util.*;

/**
 * For effects with special behavior that are unique to each card. An instance
 * of this class provides the behavior of an Effect, but also can be used to
 * construct instances of itself and can serialize itself, i.e. it is its own
 * factory.
 * 
 * @author Michael
 *
 */
public class AnonymousEffect extends Effect {
	public static final int ID = -1;
	int ownerID, anonymousIndex;

	public AnonymousEffect(String description, boolean listener) {
		super(ID, description, listener);
	}
	// override shit

	// basically a kind of "constructor", varies depending on the class
	public Effect reconstruct(StringTokenizer st) throws CloneNotSupportedException {
		return this.clone();
	}

	// basically what gets fed into the reconstruct() method
	public String toConstructorString() {
		return "";
	}

	@Override
	public String toString() {
		return super.toString() + this.ownerID + " " + this.anonymousIndex + " " + this.toConstructorString();
	}
}
