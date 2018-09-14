package server.card;

import client.tooltip.*;
import server.Board;
import server.card.effect.Effect;

public class Amulet extends BoardObject {
	public Amulet(Board b, CardStatus status, int cost, TooltipAmulet tooltip, String imagepath, int team, int id) {
		super(b, status, tooltip, imagepath, team, id);
		this.addBasicEffect(new Effect(0, "", cost));
	}
}
