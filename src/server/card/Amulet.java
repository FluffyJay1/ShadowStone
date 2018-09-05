package server.card;

import server.Board;
import server.card.effect.Effect;

public class Amulet extends BoardObject {
	public Amulet(Board b, CardStatus status, int cost, String name, String text, String imagepath, int team, int id) {
		super(b, status, name, text, imagepath, team, id);
		this.addBasicEffect(new Effect(0, "", cost));
	}
}
